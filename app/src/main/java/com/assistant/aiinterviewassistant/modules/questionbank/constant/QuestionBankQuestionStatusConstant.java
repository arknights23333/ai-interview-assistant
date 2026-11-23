package com.assistant.aiinterviewassistant.modules.questionbank.constant;

/** 题库题目状态，对应 question_bank_question.status。 */
public final class QuestionBankQuestionStatusConstant {

    /** 草稿。 */
    public static final String DRAFT = "DRAFT";

    /** 已启用，可用于专项面试。 */
    public static final String ACTIVE = "ACTIVE";

    /** 已归档。 */
    public static final String ARCHIVED = "ARCHIVED";

    private QuestionBankQuestionStatusConstant() {}
}
