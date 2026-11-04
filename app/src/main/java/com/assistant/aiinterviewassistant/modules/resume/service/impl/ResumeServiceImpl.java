package com.assistant.aiinterviewassistant.modules.resume.service.impl;

import com.assistant.aiinterviewassistant.common.exception.enums.FileErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.UserErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.infrastructure.document.service.ContentTypeDetectionService;
import com.assistant.aiinterviewassistant.infrastructure.document.service.FileHashService;
import com.assistant.aiinterviewassistant.infrastructure.storage.service.FileStorageService;
import com.assistant.aiinterviewassistant.modules.resume.constant.ResumeParseStatusConstant;
import com.assistant.aiinterviewassistant.modules.resume.domain.po.ResumePo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadVo;
import com.assistant.aiinterviewassistant.modules.resume.service.ResumeService;
import com.assistant.aiinterviewassistant.modules.resume.mapper.ResumeMapper;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.UserVo;
import com.assistant.aiinterviewassistant.modules.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, ResumePo> implements ResumeService {

    private final UserService userService;

    private final FileStorageService fileStorageService;

    private final FileHashService fileHashService;

    private final ContentTypeDetectionService contentTypeDetectionService;

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
