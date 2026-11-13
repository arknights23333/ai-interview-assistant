package com.assistant.aiinterviewassistant.modules.knowledgebase.service;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentUploadBatchVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档服务
 */
public interface DocumentService extends IService<DocumentPo> {
    /**
     * 批量文档上传对象存储
     *
     * @param userId
     * @param files
     * @param knowledgeBaseId
     * @return
     */
    List<DocumentUploadBatchVo> uploadBatch(Long userId, List<MultipartFile> files, Long knowledgeBaseId);

    /**
     * 获取指定知识库文档列表
     *
     * @param userId
     * @param knowledgeBaseId
     * @param pageQuery
     * @return
     */
    Page<DocumentVo> getList(Long userId, Long knowledgeBaseId, @Valid PageQuery pageQuery);

    /**
     * 获取下载文档预签名
     * @param userId
     * @param documentId
     * @return
     */
    String download(Long userId, Long documentId);

    /**
     * 批量删除文档
     * @param userId
     * @param knowledgeBaseId 所属知识库 ID
     * @param ids
     * @return
     */
    List<Long> deleteBatch(Long userId, Long knowledgeBaseId, List<Long> ids);
}
