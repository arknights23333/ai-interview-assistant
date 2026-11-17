package com.assistant.aiinterviewassistant.modules.interview.domain.vo;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

/** 面试页面展示的题目和作答。 */
@Getter
@Setter
public class InterviewQuestionVo {

    /** 本场题目 ID。 */
    private Long id;

    /** 主问题轮次，从 1 开始。 */
    private Integer roundNo;

    /** 轮内顺序，0 表示主问题，正整数表示第几道追问。 */
    private Integer followUpNo;

    /** 父题 ID，主问题为空。 */
    @Nullable
    private Long parentQuestionId;

    /** 本场实际使用的题干。 */
    private String question;

    /** 题目的考察分类。 */
    @Nullable
    private String category;

    /** 用户答案，未提交时为空，交白卷时为空字符串。 */
    @Nullable
    private String userAnswer;

    /** 作答提交时间，未提交时为空。 */
    @Nullable
    private OffsetDateTime answeredAt;

    /** 本场保存的参考答案，由会话服务在面试结束后展示。 */
    @Nullable
    private String referenceAnswer;

    /** 本场保存的答题要点，由会话服务在面试结束后展示。 */
    @Nullable
    private List<String> keyPoints;

    /** 本场保存的评分标准，由会话服务在面试结束后展示。 */
    @Nullable
    private String scoringRubric;
}
