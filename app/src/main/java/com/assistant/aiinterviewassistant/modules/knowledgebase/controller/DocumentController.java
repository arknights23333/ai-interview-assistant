package com.assistant.aiinterviewassistant.modules.knowledgebase.controller;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentUploadBatchVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.DocumentVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.DocumentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
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

    /**
     * 批量上传文档
     * @param jwt
     * @param files
     * @param knowledgeBaseId
     * @return
     */
    @PostMapping("/uploadBatch/{knowledgeBaseId}")
    public R<List<DocumentUploadBatchVo>> uploadBatch(
            @AuthenticationPrincipal Jwt jwt ,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @PathVariable("knowledgeBaseId") Long knowledgeBaseId
    ) {
        List<DocumentUploadBatchVo> results = documentService.uploadBatch(Long.valueOf(jwt.getSubject()), files, knowledgeBaseId);
        return R.ok(results,"批量上传文档成功");
    }
    /**
     * 获取指定知识库文档列表
     * @param jwt
     * @param knowledgeBaseId
     * @return
     */
    @PostMapping("/list/{knowledgeBaseId}")
    public R<Page<DocumentVo>> list(
            @AuthenticationPrincipal Jwt jwt ,
            @PathVariable("knowledgeBaseId") Long knowledgeBaseId,
            @Valid @ModelAttribute PageQuery pageQuery
    ) {
        Page<DocumentVo> results = documentService.getList(Long.valueOf(jwt.getSubject()), knowledgeBaseId,pageQuery);
        return R.ok(results,"获取文档列表成功");
    }

    /**
     * 获取文档下载预签名
     * @param jwt
     * @param documentId
     * @return
     */
    @GetMapping("/download/{documentId}")
    public R<String> download(
            @AuthenticationPrincipal Jwt jwt ,
            @PathVariable("documentId") Long documentId
    ){
        String result = documentService.download(Long.valueOf(jwt.getSubject()), documentId);
        return R.ok(result,"获取预签名成功");
    }

    /**
     * 批量删除文档
     * @param jwt
     * @param knowledgeBaseId
     * @param ids
     * @return
     */
    @DeleteMapping("/deleteBatch/{knowledgeBaseId}")
    public R<List<Long>> deleteBatch(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("knowledgeBaseId") Long knowledgeBaseId,
            @RequestParam(value = "ids", required = false) List<Long> ids
    ) {
        return R.ok(documentService.deleteBatch(Long.valueOf(jwt.getSubject()), knowledgeBaseId, ids));
    }
    /**
     * 删除知识库,连带删除知识库下文档
     * @param jwt
     * @param id
     * @return
     */
    @DeleteMapping("/deleteKnowledgeBase/{knowledgeBaseId}")
    public R<Void> deleteKnowledgeBase(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("knowledgeBaseId") Long id
    ){
        documentService.deleteKnowledgeBase(Long.valueOf(jwt.getSubject()), id);
        return R.ok("知识库删除成功");
    }
}
