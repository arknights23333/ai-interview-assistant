package com.assistant.aiinterviewassistant.modules.knowledgebase.controller;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseNameDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseQueryDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.KnowledgeBaseVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.KnowledgeBaseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库管理。
 *
 * @folder 知识库管理
 */
@RestController
@RequestMapping("/api/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /** 创建知识库。 */
    @PostMapping("/create")
    public R<Long> createKnowledgeBase(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody KnowledgeBaseNameDto dto) {
        return R.ok(knowledgeBaseService.createKnowledgeBase(Long.valueOf(jwt.getSubject()), dto), "创建成功");
    }

    /** 重命名知识库。 */
    @PutMapping("/update/{id}")
    public R<Void> updateKnowledgeBaseName(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable("id") Long id,
                                           @Valid @RequestBody KnowledgeBaseNameDto dto) {
        knowledgeBaseService.updateKnowledgeBaseName(Long.valueOf(jwt.getSubject()), id, dto);
        return R.ok("修改成功");
    }

    /** 分页查询知识库。 */
    @PostMapping("/list")
    public R<Page<KnowledgeBaseVo>> listKnowledgeBases(@AuthenticationPrincipal Jwt jwt,
                                                       @Valid @RequestBody KnowledgeBaseQueryDto dto,
                                                       @Valid @ModelAttribute PageQuery pageQuery) {
        return R.ok(knowledgeBaseService.listKnowledgeBases(Long.valueOf(jwt.getSubject()), dto, pageQuery));
    }
}
