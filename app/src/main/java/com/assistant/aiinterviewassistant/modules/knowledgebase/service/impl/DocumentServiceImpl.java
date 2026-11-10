package com.assistant.aiinterviewassistant.modules.knowledgebase.service.impl;
import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.FileErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.infrastructure.storage.service.FileStorageService;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentUploadBatchVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.DocumentService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.assistant.aiinterviewassistant.modules.knowledgebase.mapper.DocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, DocumentPo> implements DocumentService {

    private final FileStorageService fileStorageService;
    @Override
    public List<DocumentUploadBatchVo> uploadBatch(Long userId, List<MultipartFile> files) {


        return List.of();
    }
}
