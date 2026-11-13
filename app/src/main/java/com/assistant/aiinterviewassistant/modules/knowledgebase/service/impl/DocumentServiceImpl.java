package com.assistant.aiinterviewassistant.modules.knowledgebase.service.impl;
import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.DocumentErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.FileErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.KnowledgeBaseErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.infrastructure.document.service.ContentTypeDetectionService;
import com.assistant.aiinterviewassistant.infrastructure.document.service.FileHashService;
import com.assistant.aiinterviewassistant.infrastructure.storage.domain.StorageDeleteModels;
import com.assistant.aiinterviewassistant.infrastructure.storage.service.FileStorageService;
import com.assistant.aiinterviewassistant.modules.knowledgebase.constant.DocumentParseStatusConstant;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentUploadBatchVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.DocumentService;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.KnowledgeBasePo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.mapper.KnowledgeBaseMapper;
import com.assistant.aiinterviewassistant.modules.resume.domain.po.ResumePo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.assistant.aiinterviewassistant.modules.knowledgebase.mapper.DocumentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, DocumentPo> implements DocumentService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final FileStorageService fileStorageService;

    private final ContentTypeDetectionService typeDetector;

    private final FileHashService hashService;

    /** 文档文件最大为 50 MiB。 */
    private static final long MAX_DOCUMENT_FILE_SIZE = 50L * 1024 * 1024;
    private static final int MAX_BATCH_UPLOAD_FILES = 5;

    /** 批量解析文档，最多允许 5 份。 */
    private static final int MAX_BATCH_PARSE_DOCUMENT = 5;

    /** 允许上传的文档 MIME 类型。 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/markdown"
    );
    @Override
    public List<DocumentUploadBatchVo> uploadBatch(Long userId, List<MultipartFile> files, Long knowledgeBaseId) {
        if (files == null || files.size() == 0){
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
        if(files.size() > MAX_BATCH_UPLOAD_FILES){
            throw new BusinessException(DocumentErrorCode.BATCH_UPLOAD_LIMIT_EXCEEDED);
        }
        requireOwnedKnowledgeBase(knowledgeBaseId, userId);

        List<DocumentUploadBatchVo> results = new ArrayList<>(files.size());
        for(int i =0 ;i<files.size();i++){
            MultipartFile file = files.get(i);
            String filename = file == null ? null : file.getOriginalFilename();
            try{
                UploadOutcome outcome = uploadOne(file, userId, knowledgeBaseId);
                DocumentUploadBatchVo.Status status = outcome.reused() ?
                        DocumentUploadBatchVo.Status.REUSED : DocumentUploadBatchVo.Status.SUCCESS;
                results.add(new DocumentUploadBatchVo(
                        i,
                        outcome.filename(),
                        outcome.documentId(),
                        status,
                        null,
                        null
                ));
            }catch (BusinessException e){

                log.error("上传文档失败，filename={} userId={}", filename, userId, e);
                results.add(new DocumentUploadBatchVo(
                        i,
                        filename,
                        null,
                        DocumentUploadBatchVo.Status.FAILED,
                        String.valueOf(e.getCode()),
                        e.getMessage()
                ));
            }

        }


        return results;
    }

    @Override
    public Page<DocumentVo> getList(Long userId, Long knowledgeBaseId, @Valid PageQuery pageQuery) {
        requireOwnedKnowledgeBase(knowledgeBaseId, userId);
        Page<DocumentPo> page = new Page<DocumentPo>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<DocumentPo> wrapper = new LambdaQueryWrapper<DocumentPo>()
                .eq(DocumentPo::getKnowledgeBaseId, knowledgeBaseId)
                .orderByDesc(DocumentPo::getCreateAt, DocumentPo::getId);
        Page<DocumentPo> result = page(page, wrapper);
        List<DocumentVo> records = result.getRecords().stream().map(po -> {
            DocumentVo vo = new DocumentVo();
            BeanUtils.copyProperties(po, vo);
            return vo;
        }).toList();

        Page<DocumentVo> response = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        response.setRecords(records);
        return response;
    }

    @Override
    public String download(Long userId, Long documentId) {
        DocumentPo document = getById(documentId);
        if(document==null){
            throw new BusinessException(DocumentErrorCode.DOCUMENT_NOT_FOUND);
        }
        requireOwnedKnowledgeBase(document.getKnowledgeBaseId(), userId);

        return fileStorageService.fileDownload(document.getStorageKey(), document.getOriginalFilename());
    }

    @Override
    public List<Long> deleteBatch(Long userId, Long knowledgeBaseId, List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(DocumentErrorCode.INVALID_DOCUMENT_IDS);
        }
        requireOwnedKnowledgeBase(knowledgeBaseId, userId);

        List<Long> target = ids.stream().distinct().toList();

        List<DocumentPo> deletedPo = getBaseMapper().deleteBatchAndReturn(target, userId, knowledgeBaseId);

        Set<Long> deletedIds = deletedPo.stream()
                .map(DocumentPo::getId)
                .collect(Collectors.toSet());

        List<Long> undelete = target.stream().filter(id -> !deletedIds.contains(id)).toList();

        if(deletedPo.size() == 0){
            return undelete;
        }


        log.info("删除完成，userId={},requested={} deleted={} undeleted={}",userId,target.size(),deletedIds.size(),target.size()-deletedIds.size());

        Map<String, Long> idByKey = deletedPo.stream().
                collect(Collectors.toMap(DocumentPo::getStorageKey, DocumentPo::getId));
        try {
            StorageDeleteModels.BatchDeleteResult<Long> result = fileStorageService.fileDeleteBatch(idByKey);
            log.info("文档原文件批量清理完成，userId={} deleted={} failed={}",
                    userId, result.successIds().size(), result.failures().size());
            for(var failure : result.failures()){
                log.warn("文档原文件待补删 documentId={} storageKey={} code={} s3Code={} reason={}",
                        failure.id(), failure.key(), failure.code(), failure.s3Code(), failure.errorMessage());
            }
        }catch (RuntimeException e) {
            // 删库已经提交，保留旧对象键和对应简历 ID，便于后台人工补删。
            log.error("文档源文件清理异常，待补删对象及文档 ID={}", idByKey, e);
        }

        return undelete;

    }

    /**
     * 单个文档上传接口
     * @param file
     * @param userId
     * @param knowledgeBaseId
     * @return
     */
    private UploadOutcome uploadOne(MultipartFile file, Long userId, Long knowledgeBaseId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > MAX_DOCUMENT_FILE_SIZE) {
            throw new BusinessException(FileErrorCode.FILE_TOO_LARGE);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank() || filename.codePointCount(0, filename.length()) > 255) {
            throw new BusinessException(DocumentErrorCode.INVALID_DOCUMENT_FILENAME);
        }

        String contentType = typeDetector.detectContentType(file);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(FileErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
        String fileHash = hashService.calculateHash(file);
        DocumentPo po ;
        try {
             po = filehasExist(fileHash,knowledgeBaseId);

        } catch (RuntimeException e) {
            throw new BusinessException(DocumentErrorCode.DOCUMENT_UPLOAD_CHECK_FAILED, e);
        }
        if(po!=null){
            return new UploadOutcome(po.getId(), po.getOriginalFilename(), true);
        }
        String storageKey = fileStorageService.uploadKnowledgeBase(file);
        DocumentPo document = new DocumentPo();

        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setFileHash(fileHash);
        document.setOriginalFilename(filename);
        document.setContentType(contentType);
        document.setStorageKey(storageKey);
        document.setFileSize(file.getSize());
        document.setProcessStatus(DocumentParseStatusConstant.PENDING);

        try {
            if (!save(document)) {
                throw new IllegalStateException("保存文档记录失败");
            }
        } catch (RuntimeException e) {
            cleanupUploadedFile(storageKey, e);
            if (e instanceof DuplicateKeyException) {
                // 重复键也可能来自主键，按知识库和文件哈希确认有可复用的文档。
                try {
                    DocumentPo concurrentDocument = filehasExist(fileHash, knowledgeBaseId);
                    if (concurrentDocument != null) {
                        log.info("并发上传复用已有文档，documentId={} userId={}", concurrentDocument.getId(), userId);
                        log.debug("上传文档遇到重复键，userId={}", userId, e);
                        return new UploadOutcome(concurrentDocument.getId(), concurrentDocument.getOriginalFilename(), true);
                    }
                } catch (RuntimeException lookupException) {
                    e.addSuppressed(lookupException);
                }
            }
            throw new BusinessException(DocumentErrorCode.DOCUMENT_SAVE_FAILED, e);
        }

        log.info("文档上传并落库成功，documentId={} userId={} storageKey={} sizeBytes={}",
                document.getId(), userId, storageKey, document.getFileSize());
        return new UploadOutcome(document.getId(), filename, false);

    }

    /** 校验知识库存在且属于当前用户。 */
    private void requireOwnedKnowledgeBase(Long knowledgeBaseId, Long userId) {
        boolean owned = knowledgeBaseMapper.selectCount(new LambdaQueryWrapper<KnowledgeBasePo>()
                .eq(KnowledgeBasePo::getId, knowledgeBaseId)
                .eq(KnowledgeBasePo::getUserId, userId)) > 0;
        if (!owned) {
            throw new BusinessException(KnowledgeBaseErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
    }

    private DocumentPo filehasExist(String fileHash, Long knowledgeBaseId) {
        DocumentPo po = getOne(new LambdaQueryWrapper<DocumentPo>()
                .eq(DocumentPo::getFileHash, fileHash)
                .eq(DocumentPo::getKnowledgeBaseId, knowledgeBaseId));
        if(po==null){
            return null;
        }
        return po;
    }
    /**
     * 数据库保存失败后，尝试清理本次上传的对象。
     * 清理失败时记录日志，不覆盖原始异常。
     */
    private void cleanupUploadedFile(String fileKey, RuntimeException originalException) {
        try {
            fileStorageService.deleteKnowledgeBase(fileKey);
            log.info("已清理落库失败的文档上传对象，storageKey={}", fileKey);
        } catch (RuntimeException cleanupException) {
            originalException.addSuppressed(cleanupException);
            log.error("文档落库失败后清理对象失败，key: {}", fileKey, cleanupException);
        }
    }

    private record UploadOutcome(Long documentId, String filename, boolean reused) {
    }
}
