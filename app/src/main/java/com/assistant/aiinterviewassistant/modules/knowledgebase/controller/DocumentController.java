package com.assistant.aiinterviewassistant.modules.knowledgebase.controller;

import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentUploadBatchVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档模块接口
 * @folder 文档模块
 */
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    @PostMapping("/uploadBatch")
    public R<List<DocumentUploadBatchVo>> uploadBatch(
            @AuthenticationPrincipal Jwt jwt ,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        List<DocumentUploadBatchVo> results = documentService.uploadBatch(Long.valueOf(jwt.getSubject()), files);
        return R.ok();
    }

}
