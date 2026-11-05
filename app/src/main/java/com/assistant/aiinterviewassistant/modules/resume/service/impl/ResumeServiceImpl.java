package com.assistant.aiinterviewassistant.modules.resume.service.impl;
import com.assistant.aiinterviewassistant.modules.resume.domain.dto.DeleteResumeDto;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.FileErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.ResumeErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.infrastructure.document.service.ContentTypeDetectionService;
import com.assistant.aiinterviewassistant.infrastructure.document.service.FileHashService;
import com.assistant.aiinterviewassistant.infrastructure.document.service.DocumentParseService;
import com.assistant.aiinterviewassistant.infrastructure.storage.service.FileStorageService;
import com.assistant.aiinterviewassistant.modules.resume.constant.ResumeParseStatusConstant;
import com.assistant.aiinterviewassistant.modules.resume.domain.po.ResumePo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeVo;
import com.assistant.aiinterviewassistant.modules.resume.service.ResumeService;
import com.assistant.aiinterviewassistant.modules.resume.mapper.ResumeMapper;
import com.assistant.aiinterviewassistant.modules.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, ResumePo> implements ResumeService {

    private final UserService userService;

    private final FileStorageService fileStorageService;

    private final FileHashService fileHashService;

    private final ContentTypeDetectionService contentTypeDetectionService;

    private final DocumentParseService documentParseService;

    private final ThreadPoolExecutor documentParseThreadPoolExecutor;


    /** 简历文件最大为 10 MiB。 */
    private static final long MAX_RESUME_FILE_SIZE = 10L * 1024 * 1024;

    /** 允许上传的简历 MIME 类型。 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );
    /**
     * 上传简历到对象存储并落库（没有进行简历解析）
     * @param id 用户id
     * @param file 简历文件
     * @return
     */
    @Override
    public R<ResumeUploadVo> upload(@Nullable Long id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_RESUME_FILE_SIZE) {
            throw new BusinessException(FileErrorCode.FILE_TOO_LARGE);
        }

        String contentType = contentTypeDetectionService.detectContentType(file);

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(FileErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }

        String hash = fileHashService.calculateHash(file);

        ResumePo exist = filehasExist(hash, id);
        if(exist != null){
            return R.ok(new ResumeUploadVo(exist.getId()));
        }
        String fileKey = fileStorageService.uploadResume(file);

        ResumePo resume = new ResumePo();
        resume.setUserId(id);
        resume.setFileHash(hash);
        resume.setFileName(file.getOriginalFilename());
        resume.setFileSize(file.getSize());
        resume.setStorageKey(fileKey);
        resume.setParseStatus(ResumeParseStatusConstant.PENDING);


        resume.setContentType(contentType);
        // TODO 并发重复上传触发唯一约束时，清理本次对象后查询并返回已有有效记录。
        try {
            if (!save(resume)) {
                throw new IllegalStateException("保存简历记录失败");
            }
        } catch (RuntimeException e) {
            cleanupUploadedFile(fileKey, e);
            throw e;
        }

        return R.ok(new ResumeUploadVo(resume.getId()));
    }

    @Override
    public R<Void> parseResume(Long resumeId, Long userId) {
        ResumePo resume = getAvailableResumeOrThrow(resumeId, userId);
        if (ResumeParseStatusConstant.PROCESSING.equals(resume.getParseStatus())) {
            return R.<Void>ok("解析任务正在处理，请勿重复提交");
        }

        // 提交前占用状态，PROCESSING 同时包括排队与执行阶段。
        boolean accepted = update(new LambdaUpdateWrapper<ResumePo>()
                .eq(ResumePo::getId, resumeId)
                .eq(ResumePo::getUserId, userId)
                .isNull(ResumePo::getDeleteTime)
                .eq(ResumePo::getParseStatus, resume.getParseStatus())
                .set(ResumePo::getParseStatus, ResumeParseStatusConstant.PROCESSING)
                .set(ResumePo::getParseError, null));
        if (!accepted) {
            throw new BusinessException(FileErrorCode.FILE_PARSE_ERROR);
        }

        try {
            documentParseThreadPoolExecutor.execute(() -> {
                try {
                    // 在后台线程中完成下载、解析和清洗，不阻塞请求线程。
                    String text = documentParseService.parseContent(resume.getStorageKey());
                    if (text == null || text.isBlank()) {
                        throw new BusinessException(FileErrorCode.FILE_PARSE_ERROR);
                    }
                    boolean saved = update(new LambdaUpdateWrapper<ResumePo>()
                            .eq(ResumePo::getId, resumeId)
                            .eq(ResumePo::getUserId, userId)
                            .eq(ResumePo::getParseStatus, ResumeParseStatusConstant.PROCESSING)
                            .set(ResumePo::getResumeContent, text)
                            .set(ResumePo::getParseStatus, ResumeParseStatusConstant.SUCCESS)
                            .set(ResumePo::getParseError, null));
                    if (!saved) {
                        throw new IllegalStateException("保存简历解析结果失败");
                    }
                } catch (Exception e) {
                    log.error("简历解析失败，resumeId: {}", resumeId, e);
                    String message = e instanceof BusinessException
                            ? e.getMessage() : "文件解析失败，请稍后重试";
                    try {
                        boolean marked = update(new LambdaUpdateWrapper<ResumePo>()
                                .eq(ResumePo::getId, resumeId)
                                .eq(ResumePo::getUserId, userId)
                                .eq(ResumePo::getParseStatus, ResumeParseStatusConstant.PROCESSING)
                                .set(ResumePo::getParseStatus, ResumeParseStatusConstant.FAILED)
                                .set(ResumePo::getParseError, message));
                        if (!marked) {
                            log.warn("未更新简历解析失败状态，resumeId: {}", resumeId);
                        }
                    } catch (RuntimeException stateException) {
                        log.error("保存简历解析失败状态时出错，resumeId: {}", resumeId, stateException);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            // 任务未入队，不能留下正在处理的状态。
            log.warn("解析线程池拒绝任务，resumeId: {}", resumeId, e);
            try {
                boolean marked = update(new LambdaUpdateWrapper<ResumePo>()
                        .eq(ResumePo::getId, resumeId)
                        .eq(ResumePo::getUserId, userId)
                        .eq(ResumePo::getParseStatus, ResumeParseStatusConstant.PROCESSING)
                        .set(ResumePo::getParseStatus, ResumeParseStatusConstant.FAILED)
                        .set(ResumePo::getParseError, "解析任务繁忙，请稍后重试"));
                if (!marked) {
                    log.warn("未更新解析任务被拒绝后的失败状态，resumeId: {}", resumeId);
                }
            } catch (RuntimeException stateException) {
                e.addSuppressed(stateException);
                log.error("保存解析任务被拒绝后的失败状态时出错，resumeId: {}", resumeId, stateException);
            }
            throw new BusinessException(FileErrorCode.FILE_PARSE_ERROR, e);
        }

        return R.<Void>ok("解析任务已提交");
    }

    @Override
    public R<ResumeVo> getResume(Long resumeId, Long userId) {
        ResumePo resume = getAvailableResumeOrThrow(resumeId, userId);
        ResumeVo vo =new ResumeVo();
        vo.setId(resume.getId());
        vo.setResumeContent(resume.getResumeContent());
        vo.setCreateAt(resume.getCreateAt());
        vo.setFileName(resume.getFileName());
        vo.setFileSize(resume.getFileSize());
        vo.setParseStatus(resume.getParseStatus());
        vo.setParseError(resume.getParseError());
        vo.setContentType(resume.getContentType());
        return R.ok(vo);
    }

    @Override
    public R<Page<ResumeVo>> getResumeList(Long userId, PageQuery pageQuery) {
        Page<ResumePo> page =new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<ResumePo> result=page(page,new LambdaQueryWrapper<ResumePo>()
                .eq(ResumePo::getUserId, userId)
                .isNull(ResumePo::getDeleteTime)
                .orderByDesc(ResumePo::getCreateAt)
                .orderByDesc(ResumePo::getId));
        return R.ok(convertPage(result, ResumeVo.class));
    }

    @Override
    public R<Void> updateRecycleStatus(Long resumeId, Boolean recycleStatus, Long userId) {
        ResumePo resume=getOne(new LambdaQueryWrapper<ResumePo>()
                .eq(ResumePo::getId, resumeId)
                .eq(ResumePo::getUserId, userId ));
        if(resume==null){
            throw new BusinessException(ResumeErrorCode.RESUME_NOT_FOUND);
        }
        // 已经处于目标状态，直接返回，不重复更新删除时间
        boolean alreadyRestored = recycleStatus && resume.getDeleteTime() == null;
        boolean alreadyRecycled = !recycleStatus && resume.getDeleteTime() != null;

        if (alreadyRestored || alreadyRecycled) {
            return R.<Void>ok("简历已处于目标状态");
        }

        if(recycleStatus){

            boolean result = update(new LambdaUpdateWrapper<ResumePo>()
                    .eq(ResumePo::getId, resumeId)
                    .set(ResumePo::getDeleteTime, null));
            if(!result){
                throw new BusinessException(ResumeErrorCode.RESUME_UPDATE_FAILED);
            }
        }
        else {
            boolean result = update(new LambdaUpdateWrapper<ResumePo>()
                    .eq(ResumePo::getId, resumeId)
                    .set(ResumePo::getDeleteTime, OffsetDateTime.now()));
            if(!result){
                throw new BusinessException(ResumeErrorCode.RESUME_UPDATE_FAILED);
            }
        }
        return R.<Void>ok("回收站状态更新成功");
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public R<List<Long>> deleteResume(DeleteResumeDto dto, Long userId) {
        List<Long> resumeIds = dto.getResumeIds();
        boolean hasResumeIds = resumeIds != null && !resumeIds.isEmpty();
        // 必须且只能选择一种删除范围。
        if (dto.isDeleteAll() == hasResumeIds) {
            throw new BusinessException(ResumeErrorCode.INVALID_DELETE_SCOPE);
        }

        // 清空回收站时先确定本次处理的 ID 范围，用于计算未删掉的记录。
        List<Long> targetIds = dto.isDeleteAll()
                ? list(new LambdaQueryWrapper<ResumePo>()
                        .select(ResumePo::getId)
                        .eq(ResumePo::getUserId, userId)
                        .isNotNull(ResumePo::getDeleteTime))
                        .stream().map(ResumePo::getId).toList()
                : resumeIds.stream().distinct().toList();
        if (targetIds.isEmpty()) {
            return R.ok(List.of());
        }

        // 在事务外执行单条删除语句。Mapper 返回后，再清理已提交删除的对象。
        List<ResumePo> deletedResumes = getBaseMapper().deleteRecycledReturning(userId, targetIds);
        Set<Long> deletedIds = deletedResumes.stream()
                .map(ResumePo::getId)
                .collect(Collectors.toSet());
        List<Long> undeletedIds = targetIds.stream()
                .filter(id -> !deletedIds.contains(id))
                .toList();

        if (!deletedResumes.isEmpty()) {
            Map<String, Long> idByKey = deletedResumes.stream()
                    .collect(Collectors.toMap(ResumePo::getStorageKey, ResumePo::getId));
            try {
                var storageResult = fileStorageService.fileDeleteBatch(idByKey);
                for (var failure : storageResult.failures()) {
                    log.warn("简历原文件待补删 resumeId={} storageKey={} code={} s3Code={} reason={}",
                            failure.id(), failure.key(), failure.code(), failure.s3Code(), failure.errorMessage());
                }
            } catch (RuntimeException e) {
                // 删库已经提交，保留旧对象键和对应简历 ID，便于后台人工补删。
                log.error("简历原文件清理异常，待补删对象及简历 ID={}", idByKey, e);
            }
        }
        return R.ok(undeletedIds);
    }

    /**
     * 获取当前用户未删除的简历，不存在或不可访问时抛出业务异常。
     * 此处不要求正文解析成功，具体解析状态由调用方判断。
     *
     * @param resumeId 简历 ID
     * @param userId 当前用户 ID
     * @return 当前用户可访问且未删除的简历
     */
    private ResumePo getAvailableResumeOrThrow(Long resumeId, Long userId) {
        ResumePo resume = getOne(new LambdaQueryWrapper<ResumePo>()
                .eq(ResumePo::getId, resumeId)
                .eq(ResumePo::getUserId, userId)
                .isNull(ResumePo::getDeleteTime));

        if (resume == null) {
            throw new BusinessException(ResumeErrorCode.RESUME_NOT_FOUND);
        }
        return resume;
    }
    /**
     * 保留分页信息，并将当前页记录逐条属性对拷为目标类型。
     *
     * @param source 原分页结果
     * @param targetType 具有可用无参构造器的目标类型
     * @param <S> 原记录类型
     * @param <T> 目标记录类型
     * @return 转换后的分页结果
     */
    private <S, T> Page<T> convertPage(Page<S> source, Class<T> targetType) {
        return new Page<T>(
                source.getCurrent(),
                source.getSize(),
                source.getTotal()
        ).setRecords(source.getRecords().stream()
                .map(item -> {
                    T target = BeanUtils.instantiateClass(targetType);
                    BeanUtils.copyProperties(item, target);
                    return target;
                })
                .toList());
    }
    /**
     * 检查文件是否已存在
     * @param hash
     * @param id
     * @return
     */
    public ResumePo filehasExist(String hash, Long id){
        ResumePo resume = getOne(new LambdaQueryWrapper<ResumePo>()
                .eq(ResumePo::getFileHash, hash)
                .eq(ResumePo::getUserId, id)
                .last("ORDER BY delete_time DESC NULLS FIRST, id DESC LIMIT 1"));

        if (resume == null) {
            return null;
        }
        if(resume.getDeleteTime() != null ){
            // TODO 并发恢复触发唯一约束时，查询并返回已有有效记录。
            boolean restored = update(new LambdaUpdateWrapper<ResumePo>()
                    .eq(ResumePo::getId, resume.getId())
                    .set(ResumePo::getDeleteTime, null));
            if (!restored) {
                throw new IllegalStateException("恢复简历记录失败");
            }
            resume.setDeleteTime(null);
        }
        return resume;
    }
    /**
     * 数据库保存失败后，尝试清理本次上传的对象。
     * 清理失败时记录日志，不覆盖原始异常。
     */
    private void cleanupUploadedFile(String fileKey, RuntimeException originalException) {
        try {
            fileStorageService.deleteResume(fileKey);
        } catch (RuntimeException cleanupException) {
            originalException.addSuppressed(cleanupException);
            log.error("简历落库失败后清理对象失败，key: {}", fileKey, cleanupException);
        }
    }
}
