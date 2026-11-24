package com.assistant.aiinterviewassistant.modules.questionbank.controller;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.CreateQuestionDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.QuestionBankQueryDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.vo.QuestionBankQuestionDetailVo;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.vo.QuestionBankQuestionListVo;
import com.assistant.aiinterviewassistant.modules.questionbank.service.QuestionBankQuestionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

    /** 分页查询本人指定知识库下的题目，支持按方向、难度和状态筛选。 */
    @PostMapping("/list")
    public R<Page<QuestionBankQuestionListVo>> listQuestions(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody QuestionBankQueryDto dto,
            @Valid @ModelAttribute PageQuery pageQuery) {
        return R.ok(questionBankQuestionService.listQuestions(Long.valueOf(jwt.getSubject()), dto, pageQuery));
    }

    /** 查询本人有权访问的单道题或修改草稿。 */
    @GetMapping("/detail/{questionId}")
    public R<QuestionBankQuestionDetailVo> getQuestionDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("questionId") Long questionId) {
        return R.ok(questionBankQuestionService.getQuestionDetail(Long.valueOf(jwt.getSubject()), questionId));
    }
}
