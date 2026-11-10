package com.assistant.aiinterviewassistant.modules.knowledgebase.service;

import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentUploadBatchVo;
import com.baomidou.mybatisplus.spring.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档服务
 */
public interface DocumentService extends IService<DocumentPo> {
    /**
     * 批量文档上传对象存储
     * @param userId
     * @param files
     * @return
     */
    List<DocumentUploadBatchVo> uploadBatch(Long userId, List<MultipartFile> files);
}
