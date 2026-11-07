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
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeParseBatchVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeParseBatchVo.ParseSubmitOutcome;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadBatchVo;
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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private static final int MAX_BATCH_UPLOAD_FILES = 5;

    /** 批量解析简历，最多允许 5 份。 */
    private static final int MAX_BATCH_PARSE_RESUME = 5;

    /** 允许上传的简历 MIME 类型。 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/markdown"
    );
    /** 上传一份简历，返回新建或复用的简历 ID。 */
    @Override
    public R<ResumeUploadVo> upload(@Nullable Long id, MultipartFile file) {
        UploadOutcome outcome = uploadOne(id, file);
        String message = outcome.reused() ? "已复用原来简历" : "新简历上传成功";
        return R.ok(new ResumeUploadVo(outcome.resumeId()), message);
    }

    /** 逐份上传简历，按请求顺序返回每份文件的结果。 */
    @Override
    public R<List<ResumeUploadBatchVo>> uploadBatch(Long userId, List<MultipartFile> files) {
        log.info("开始批量上传简历，userId={}", userId);
        if (files == null || files.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
        if (files.size() > MAX_BATCH_UPLOAD_FILES) {
            throw new BusinessException(ResumeErrorCode.BATCH_UPLOAD_LIMIT_EXCEEDED);
        }

        List<ResumeUploadBatchVo> uploadResults = new ArrayList<>(files.size());
        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            String filename = file == null ? null : file.getOriginalFilename();
            try {
                UploadOutcome outcome = uploadOne(userId, file);
                ResumeUploadBatchVo.Status status = outcome.reused()
                        ? ResumeUploadBatchVo.Status.REUSED : ResumeUploadBatchVo.Status.SUCCESS;
                uploadResults.add(new ResumeUploadBatchVo(index, filename, outcome.resumeId(), status, null, null));
            } catch (BusinessException e) {
                if (e.getHttpStatus().is5xxServerError()) {
                    log.error("批量上传简历失败，userId={} index={} code={}", userId, index, e.getCode(), e);
                }
                uploadResults.add(new ResumeUploadBatchVo(
                        index,
                        e.getCode() == FileErrorCode.FILE_EMPTY.getCode() ? null : filename,
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(e.getCode()),
                        e.getMessage()));
            }
        }
        return R.ok(uploadResults, "批量上传成功，结果如下");
    }

    /** 校验、查重并保存一份简历。 */
    private UploadOutcome uploadOne(@Nullable Long userId, @Nullable MultipartFile file) {
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

        String hash = fileHashService.calculateHashWithFilename(file);
        ResumePo existingResume;
        try {
            existingResume = filehasExist(hash, userId);
        } catch (RuntimeException e) {
            throw new BusinessException(ResumeErrorCode.RESUME_UPLOAD_CHECK_FAILED, e);
        }
        if (existingResume != null) {
            log.info("简历上传复用已有记录，resumeId={} userId={}", existingResume.getId(), userId);
            return new UploadOutcome(existingResume.getId(), true);
        }

        String fileKey = fileStorageService.uploadResume(file);
        ResumePo resume = new ResumePo();
        resume.setUserId(userId);
        resume.setFileHash(hash);
        resume.setFileName(file.getOriginalFilename());
        resume.setFileSize(file.getSize());
        resume.setStorageKey(fileKey);
        resume.setContentType(contentType);
        resume.setParseStatus(ResumeParseStatusConstant.PENDING);

        try {
            if (!save(resume)) {
                throw new IllegalStateException("保存简历记录失败");
            }
        } catch (RuntimeException e) {
            cleanupUploadedFile(fileKey, e);
            if (e instanceof DuplicateKeyException) {
                // 重复键也可能来自主键，按用户和文件哈希确认有可复用的简历。
                try {
                    ResumePo concurrentResume = filehasExist(hash, userId);
                    if (concurrentResume != null) {
                        log.info("并发上传复用已有简历，resumeId={} userId={}", concurrentResume.getId(), userId);
                        log.debug("上传简历遇到重复键，userId={}", userId, e);
                        return new UploadOutcome(concurrentResume.getId(), true);
                    }
                } catch (RuntimeException lookupException) {
                    e.addSuppressed(lookupException);
                }
            }
            throw new BusinessException(ResumeErrorCode.RESUME_SAVE_FAILED, e);
        }

        log.info("简历上传并落库成功，resumeId={} userId={} storageKey={} sizeBytes={}",
                resume.getId(), userId, fileKey, resume.getFileSize());
        return new UploadOutcome(resume.getId(), false);
    }

    private record UploadOutcome(Long resumeId, boolean reused) {
    }

    @Override
    public R<Void> parseResume(Long resumeId, Long userId) {
        ResumePo resume = getAvailableResumeOrThrow(resumeId, userId);
        ParseSubmitOutcome outcome = submitParse(resume, userId);
        return R.<Void>ok(outcome.getMessage());
    }

    @Override
    public R<List<ResumeParseBatchVo>> parseResumeBatch(List<Long> resumeIds, Long userId) {
        log.info("开始批量解析简历，userId={}", userId);
        List<Long> targetIds = resumeIds.stream()
                .distinct()
                .toList();
        if(targetIds.size()==0||targetIds.size()>MAX_BATCH_PARSE_RESUME){
            throw  new BusinessException(ResumeErrorCode.INVALID_BATCH_PARSE_COUNT);
        }
        List<ResumeParseBatchVo> results = new ArrayList<>();
        for (Long resumeId : targetIds) {
            try {
                if (resumeId == null || resumeId <= 0) {
                    throw new BusinessException(ResumeErrorCode.INVALID_RESUME_ID);
                }

                ResumePo resume = getAvailableResumeOrThrow(resumeId, userId);
                ParseSubmitOutcome outcome = submitParse(resume, userId);
                results.add(new ResumeParseBatchVo(resumeId, outcome, null, null));
            } catch (BusinessException e) {
                results.add(new ResumeParseBatchVo(
                        resumeId, ParseSubmitOutcome.FAILED, String.valueOf(e.getCode()), e.getMessage()));
            } catch (DataAccessException e) {
                log.error("批量提交解析任务时数据库操作失败，userId={} resumeId={}", userId, resumeId, e);
                ResumeErrorCode error = ResumeErrorCode.PARSE_SUBMIT_DB_FAILED;
                results.add(new ResumeParseBatchVo(
                        resumeId, ParseSubmitOutcome.FAILED, String.valueOf(error.getCode()), error.getMessage()));
            }
        }

        return R.ok(results,"批量解析已提交");
    }

    private ParseSubmitOutcome submitParse(ResumePo resume, Long userId) {
        Long resumeId = resume.getId();
        if (ResumeParseStatusConstant.PROCESSING.equals(resume.getParseStatus())) {
            log.info("简历解析任务已在处理，跳过重复提交，resumeId: {}", resumeId);
            return ParseSubmitOutcome.ALREADY_PROCESSING;
        }
        if (ResumeParseStatusConstant.SUCCESS.equals(resume.getParseStatus())) {
            log.info("简历解析成功，跳过重复提交，resumeId: {}", resumeId);
            return ParseSubmitOutcome.ALREADY_PARSED;
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
            log.warn("简历解析任务未提交，状态更新未命中，resumeId={} userId={} expectedStatus={}",
                    resumeId, userId, resume.getParseStatus());
            throw new BusinessException(ResumeErrorCode.RESUME_STATE_CHANGED);
        }

        try {
            documentParseThreadPoolExecutor.execute(() -> {
                try {
                    log.info("开始执行简历解析任务，resumeId: {}", resumeId);
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
                    log.info("简历解析成功，结果已保存，resumeId: {}", resumeId);
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
                        .set(ResumePo::getParseError, ResumeErrorCode.PARSE_TASK_BUSY.getMessage()));
                if (!marked) {
                    log.warn("未更新解析任务被拒绝后的失败状态，resumeId: {}", resumeId);
                }
            } catch (RuntimeException stateException) {
                e.addSuppressed(stateException);
                log.error("保存解析任务被拒绝后的失败状态时出错，resumeId: {}", resumeId, stateException);
            }
            throw new BusinessException(ResumeErrorCode.PARSE_TASK_BUSY, e);
        }

        log.info("简历解析任务已提交，resumeId: {}，原解析状态: {}", resumeId, resume.getParseStatus());
        return ParseSubmitOutcome.SUBMITTED;
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
    public R<Void> updateRecycleStatus(Long resumeId, boolean recycleStatus, Long userId) {
        R<List<Long>> result = updateRecycleStatusBatch(List.of(resumeId), recycleStatus, userId);
        if (!result.getData().isEmpty()) {
            throw new BusinessException(ResumeErrorCode.RESUME_NOT_FOUND);
        }
        return R.<Void>ok(result.getMsg());
    }

    @Override
    public R<List<Long>> updateRecycleStatusBatch(List<Long> resumeIds, boolean recycleStatus, Long userId) {
        if (resumeIds == null || resumeIds.isEmpty()) {
            throw new BusinessException(ResumeErrorCode.RESUME_NOT_FOUND);
        }
        List<Long> targetIds = resumeIds.stream().distinct().toList();

        // 只更新需要切换状态的记录，重复移入时保留原删除时间。
        int updatedRows = getBaseMapper().update(null, new LambdaUpdateWrapper<ResumePo>()
                .eq(ResumePo::getUserId, userId)
                .in(ResumePo::getId, targetIds)
                .isNotNull(recycleStatus, ResumePo::getDeleteTime)
                .isNull(!recycleStatus, ResumePo::getDeleteTime)
                .set(ResumePo::getDeleteTime, recycleStatus ? null : OffsetDateTime.now()));

        List<Long> failedIds = List.of();
        if (updatedRows < targetIds.size()) {
            // 已经处于目标状态的简历也算成功。
            Set<Long> reachedTargetIds = list(new LambdaQueryWrapper<ResumePo>()
                    .select(ResumePo::getId)
                    .eq(ResumePo::getUserId, userId)
                    .in(ResumePo::getId, targetIds)
                    .isNull(recycleStatus, ResumePo::getDeleteTime)
                    .isNotNull(!recycleStatus, ResumePo::getDeleteTime))
                    .stream()
                    .map(ResumePo::getId)
                    .collect(Collectors.toSet());
            failedIds = targetIds.stream().filter(id -> !reachedTargetIds.contains(id)).toList();
        }

        log.info("简历回收站操作执行完成，userId={} restore={} requested={} updated={} failed={}",
                userId, recycleStatus, targetIds.size(), updatedRows, failedIds.size());
        if (!failedIds.isEmpty()) {
            return R.ok(failedIds, "有简历未达到目标回收站状态");
        }
        return R.ok(failedIds, updatedRows == 0 ? "简历已处于目标状态" : "回收站状态更新成功");
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
            log.info("回收站没有待删除的简历，userId={}", userId);
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
        log.info("简历数据库删除完成，userId={} deleteAll={} requested={} deleted={} undeleted={}",
                userId, dto.isDeleteAll(), targetIds.size(), deletedIds.size(), undeletedIds.size());

        if (!deletedResumes.isEmpty()) {
            Map<String, Long> idByKey = deletedResumes.stream()
                    .collect(Collectors.toMap(ResumePo::getStorageKey, ResumePo::getId));
            try {
                var storageResult = fileStorageService.fileDeleteBatch(idByKey);
                log.info("简历原文件批量清理完成，userId={} deleted={} failed={}",
                        userId, storageResult.successIds().size(), storageResult.failures().size());
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
            log.info("重复上传已恢复回收站中的简历，resumeId={} userId={}", resume.getId(), id);
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
            log.info("已清理落库失败的简历上传对象，storageKey={}", fileKey);
        } catch (RuntimeException cleanupException) {
            originalException.addSuppressed(cleanupException);
            log.error("简历落库失败后清理对象失败，key: {}", fileKey, cleanupException);
        }
    }
}
