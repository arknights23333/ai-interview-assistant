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

        String hash = fileHashService.calculateHashWithFilename(file);

        ResumePo exist = filehasExist(hash, id);
        if(exist != null){
            log.info("简历上传复用已有记录，resumeId={} userId={}", exist.getId(), id);
            return R.ok(new ResumeUploadVo(exist.getId()), "已复用原来简历");
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

        log.info("简历上传并落库成功，resumeId={} userId={} storageKey={} sizeBytes={}",
                resume.getId(), id, fileKey, file.getSize());
        return R.ok(new ResumeUploadVo(resume.getId()), "新简历上传成功");
    }

    /**
     * 批量上传简历到对象存储并落库（没有进行简历解析）
     * @param userId 用户id
     * @param files 简历文件列表
     * @return
     */
    @Override
    public R<List<ResumeUploadBatchVo>> uploadBatch(Long userId, List<MultipartFile> files) {
        log.info("开始批量上传简历，userId: {}", userId);
        if (files == null || files.isEmpty()) {
            log.error("你上传了个滚木啊!");
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
        if (files.size() > 5) {
            log.error("批量上传简历文件数量超出限制，一次最多上传五份简历");
            throw new BusinessException(ResumeErrorCode.BATCH_UPLOAD_LIMIT_EXCEEDED);
        }
        List<ResumeUploadBatchVo> uploadResults = new ArrayList<>();
        String[] hashes = new String[files.size()];
        String[] contentTypes = new String[files.size()];
        for(int i=0;i<files.size();i++){
            MultipartFile file = files.get(i);
            if(file == null || file.isEmpty()){
                log.error("单个文件为滚木");
                uploadResults.add(new ResumeUploadBatchVo(
                        i,
                        null,
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(FileErrorCode.FILE_EMPTY.getCode()),
                        FileErrorCode.FILE_EMPTY.getMessage()));
                continue;
            }
            if(file.getSize() > MAX_RESUME_FILE_SIZE){
                log.error("文件大小超出限制");
                uploadResults.add(new ResumeUploadBatchVo(
                        i,
                        file.getOriginalFilename(),
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(FileErrorCode.FILE_TOO_LARGE.getCode()),
                        FileErrorCode.FILE_TOO_LARGE.getMessage()));
                continue;
            }
            String contentType;
            try{
                contentType = contentTypeDetectionService.detectContentType(file);
            } catch (BusinessException e) {
                log.error("文件类型检测失败");
                uploadResults.add(new ResumeUploadBatchVo(
                        i,
                        file.getOriginalFilename(),
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(e.getCode()),
                        e.getMessage()));
                continue;
            }
            if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
                log.error("文件类型不支持");
                uploadResults.add(new ResumeUploadBatchVo(
                        i,
                        file.getOriginalFilename(),
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(FileErrorCode.FILE_TYPE_NOT_SUPPORTED.getCode()),
                        FileErrorCode.FILE_TYPE_NOT_SUPPORTED.getMessage()));
                continue;
            }

            contentTypes[i] = contentType;
            String hash ;
            try{
                hash= fileHashService.calculateHashWithFilename(file);
            }catch (BusinessException e){
                log.error("批量上传计算文件哈希失败，userId={} index={}", userId, i, e);
                uploadResults.add(new ResumeUploadBatchVo(
                        i,
                        file.getOriginalFilename(),
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(e.getCode()),
                        e.getMessage()));
                continue;
            }
            hashes[i] = hash;
            uploadResults.add(new ResumeUploadBatchVo(
                    i,
                    file.getOriginalFilename(),
                    null,
                    null,
                    null,
                    null));
        }
        //遍历并上传落库
        for (ResumeUploadBatchVo result : uploadResults) {
            if (result.status() == ResumeUploadBatchVo.Status.FAILED) {
                continue;
            }

            int index = result.index();
            String hash = hashes[index];
            // 每份文件上传前查重，同一批里后面的文件可以复用前面刚保存的记录。
            ResumePo exist;
            try {
                exist = filehasExist(hash, userId);
            } catch (RuntimeException e) {
                log.error("批量上传检查或恢复已有简历失败，userId={} index={}", userId, index, e);
                ResumeErrorCode error = ResumeErrorCode.RESUME_UPLOAD_CHECK_FAILED;
                uploadResults.set(index, new ResumeUploadBatchVo(
                        index,
                        result.filename(),
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(error.getCode()),
                        error.getMessage()));
                continue;
            }
            if (exist != null) {
                log.info("简历上传复用已有记录，resumeId={} userId={}", exist.getId(), userId);
                uploadResults.set(index, new ResumeUploadBatchVo(
                        index,
                        result.filename(),
                        exist.getId(),
                        ResumeUploadBatchVo.Status.REUSED,
                        null,
                        null));
                continue;
            }

            log.info("开始上传");
            ResumePo resume = new ResumePo();
            try{
                //上传对象存储
                String key = fileStorageService.uploadResume(files.get(result.index()));
                log.info("上传对象存储成功！userId={},index={},storageKey={}", userId, result.index(), key);
                resume.setUserId(userId);
                resume.setFileHash(hash);
                resume.setFileName(files.get(result.index()).getOriginalFilename());
                resume.setFileSize(files.get(result.index()).getSize());
                resume.setContentType(contentTypes[result.index()]);
                resume.setStorageKey(key);
                resume.setParseStatus(ResumeParseStatusConstant.PENDING);

            } catch (BusinessException e){
                log.error("上传对象存储失败！userId={},index={},code={}", userId, result.index(), e.getCode(),e);
                uploadResults.set(result.index(),new ResumeUploadBatchVo(
                        result.index(),
                        files.get(result.index()).getOriginalFilename(),
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(e.getCode()),
                        e.getMessage()));
                continue;
            }
            try {
                if (!save(resume)) {
                    throw new IllegalStateException("保存简历记录失败");
                }
            } catch (RuntimeException e) {
                cleanupUploadedFile(resume.getStorageKey(), e);
                if (e instanceof DuplicateKeyException) {
                    // 重复键也可能来自主键，按用户和文件哈希确认有可复用的简历。
                    try {
                        ResumePo concurrentResume = filehasExist(hash, userId);
                        if (concurrentResume != null) {
                            log.info("并发上传复用已有简历，resumeId={} userId={}", concurrentResume.getId(), userId);
                            log.debug("批量上传遇到重复键，userId={} index={}", userId, index, e);
                            uploadResults.set(index, new ResumeUploadBatchVo(
                                    index,
                                    result.filename(),
                                    concurrentResume.getId(),
                                    ResumeUploadBatchVo.Status.REUSED,
                                    null,
                                    null));
                            continue;
                        }
                    } catch (RuntimeException lookupException) {
                        e.addSuppressed(lookupException);
                    }
                }
                log.error("批量上传保存简历失败，userId={} index={}", userId, index, e);
                ResumeErrorCode error = ResumeErrorCode.RESUME_SAVE_FAILED;
                uploadResults.set(index, new ResumeUploadBatchVo(
                        index,
                        result.filename(),
                        null,
                        ResumeUploadBatchVo.Status.FAILED,
                        String.valueOf(error.getCode()),
                        error.getMessage()));
                continue;
            }

            log.info("简历上传并落库成功，resumeId={} userId={} storageKey={} sizeBytes={}",
                    resume.getId(), userId, resume.getStorageKey(), resume.getFileSize());
            uploadResults.set(index, new ResumeUploadBatchVo(
                    index,
                    result.filename(),
                    resume.getId(),
                    ResumeUploadBatchVo.Status.SUCCESS,
                    null,
                    null));

        }

        return R.ok(uploadResults);
    }

    @Override
    public R<Void> parseResume(Long resumeId, Long userId) {
        ResumePo resume = getAvailableResumeOrThrow(resumeId, userId);
        if (ResumeParseStatusConstant.PROCESSING.equals(resume.getParseStatus())) {
            log.info("简历解析任务已在处理，跳过重复提交，resumeId: {}", resumeId);
            return R.<Void>ok("解析任务正在处理，请勿重复提交");
        }
        if (ResumeParseStatusConstant.SUCCESS.equals(resume.getParseStatus())) {
            log.info("简历解析成功，跳过重复提交，resumeId: {}", resumeId);
            return R.<Void>ok("解析任务已完成，请勿重复提交");
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
            throw new BusinessException(FileErrorCode.FILE_PARSE_ERROR);
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

        log.info("简历解析任务已提交，resumeId: {}，原解析状态: {}", resumeId, resume.getParseStatus());
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
    @Transactional
    public R<Void> updateRecycleStatus(List<Long> resumeIds, boolean recycleStatus, Long userId) {
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

        if (updatedRows < targetIds.size()) {
            // 未更新的记录可能已处于目标状态，也可能不存在或不属于当前用户。
            long reachedTarget = count(new LambdaQueryWrapper<ResumePo>()
                    .eq(ResumePo::getUserId, userId)
                    .in(ResumePo::getId, targetIds)
                    .isNull(recycleStatus, ResumePo::getDeleteTime)
                    .isNotNull(!recycleStatus, ResumePo::getDeleteTime));
            if (reachedTarget != targetIds.size()) {
                log.warn("回收站操作未覆盖全部简历，事务将回滚，userId={} restore={} requested={} reachedTarget={}",
                        userId, recycleStatus, targetIds.size(), reachedTarget);
                throw new BusinessException(ResumeErrorCode.RESUME_NOT_FOUND);
            }
        }

        log.info("简历回收站操作执行完成，userId={} restore={} requested={} updated={}",
                userId, recycleStatus, targetIds.size(), updatedRows);
        return R.<Void>ok(updatedRows == 0 ? "简历已处于目标状态" : "回收站状态更新成功");
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
