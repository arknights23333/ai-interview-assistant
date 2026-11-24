package com.assistant.aiinterviewassistant.modules.questionbank.domain.vo;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** 完整的题目或修改草稿内容，用于查看与编辑。 */
@Getter
@Setter
public class QuestionBankQuestionDetailVo extends QuestionBankQuestionListVo {

    /** 参考答案，修改草稿中可为空。 */
    @Nullable
    private String referenceAnswer;

    /** 评分要点，直接以数组返回。 */
    private List<String> keyPoints;

    /** 单题评分标准，可为空。 */
    @Nullable
    private String scoringRubric;
}
