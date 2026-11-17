package com.assistant.aiinterviewassistant.modules.interview.domain.dto;

import jakarta.validation.constraints.NotBlank;

/** 按出题顺序传给题目服务的主问题草稿。 */
public record MainQuestionDraft(
        @NotBlank(message = "题目内容不能为空")
        String question,
        String category,
        String referenceAnswer,
        String keyPointsJson,
        String scoringRubric,
        String followUpCandidatesJson
) {
}
