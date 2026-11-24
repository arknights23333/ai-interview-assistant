package com.assistant.aiinterviewassistant.modules.questionbank.controller;

import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.CreateQuestionDto;
import com.assistant.aiinterviewassistant.modules.questionbank.service.QuestionBankQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库题库接口。
 *
 * @folder 题库管理
 */
@RestController
@RequestMapping("/api/question-bank")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankQuestionService questionBankQuestionService;

    /**
     * 手动创建题目，保存成功后直接启用。
     *
     * @param jwt 当前用户的登录凭证
     * @param dto 完整的新题内容
     * @return 新题 ID
     */
    @PostMapping("/create")
    public R<Long> createQuestion(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateQuestionDto dto
    ){

        return R.ok(questionBankQuestionService.create(dto,Long.valueOf(jwt.getSubject())));
    }
}
