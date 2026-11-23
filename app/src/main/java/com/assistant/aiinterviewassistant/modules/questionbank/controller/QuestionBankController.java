package com.assistant.aiinterviewassistant.modules.questionbank.controller;

import com.assistant.aiinterviewassistant.modules.questionbank.service.QuestionBankQuestionService;
import lombok.RequiredArgsConstructor;
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
}
