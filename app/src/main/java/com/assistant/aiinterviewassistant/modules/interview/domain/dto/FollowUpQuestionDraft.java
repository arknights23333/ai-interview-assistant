package com.assistant.aiinterviewassistant.modules.interview.domain.dto;

import jakarta.validation.constraints.NotBlank;

/** 现场生成的追问题干与对应评分材料。 */
public record FollowUpQuestionDraft(
        @NotBlank(message = "追问题干不能为空")
        String question,
        String referenceAnswer,
        String keyPointsJson,
        String scoringRubric
) {
}
