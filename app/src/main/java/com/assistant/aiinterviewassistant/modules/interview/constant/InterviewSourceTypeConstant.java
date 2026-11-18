package com.assistant.aiinterviewassistant.modules.interview.constant;

/** 面试题目来源，对应 interview_session.source_type。 */
public final class InterviewSourceTypeConstant {

    /** 普通文字面试。 */
    public static final String NORMAL = "NORMAL";

    /** 知识库专项面试。 */
    public static final String KNOWLEDGE_BASE = "KNOWLEDGE_BASE";

    /** 请求参数允许的题目来源。 */
    public static final String VALID_PATTERN = NORMAL + "|" + KNOWLEDGE_BASE;

    private InterviewSourceTypeConstant() {}
}
