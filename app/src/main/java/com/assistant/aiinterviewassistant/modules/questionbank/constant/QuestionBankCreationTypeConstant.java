package com.assistant.aiinterviewassistant.modules.questionbank.constant;

/** 题目最初的创建方式，对应 question_bank_question.creation_type。 */
public final class QuestionBankCreationTypeConstant {

    /** 用户手动录入。 */
    public static final String MANUAL = "MANUAL";

    /** 基于来源文档由 AI 生成。 */
    public static final String AI_GENERATED = "AI_GENERATED";

    private QuestionBankCreationTypeConstant() {}
}
