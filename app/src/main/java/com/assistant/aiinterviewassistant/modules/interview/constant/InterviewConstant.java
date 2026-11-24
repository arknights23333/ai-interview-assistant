package com.assistant.aiinterviewassistant.modules.interview.constant;

/** 面试模块的会话状态、题目来源与难度。 */
public final class InterviewConstant {

    private InterviewConstant() {}

    /** 文字面试会话状态，对应 interview_session.status。 */
    public static final class Status {

        /** 面试进行中。 */
        public static final String IN_PROGRESS = "IN_PROGRESS";

        /** 面试已结束。 */
        public static final String COMPLETED = "COMPLETED";

        private Status() {}
    }

    /** 面试题目来源，对应 interview_session.source_type。 */
    public static final class SourceType {

        /** 普通文字面试。 */
        public static final String NORMAL = "NORMAL";

        /** 知识库专项面试。 */
        public static final String KNOWLEDGE_BASE = "KNOWLEDGE_BASE";

        /** 请求参数允许的题目来源。 */
        public static final String VALID_PATTERN = NORMAL + "|" + KNOWLEDGE_BASE;

        private SourceType() {}
    }

    /** 面试难度，对应 interview_session.difficulty。 */
    public static final class Difficulty {

        /** 简单。 */
        public static final String EASY = "EASY";

        /** 中等。 */
        public static final String MEDIUM = "MEDIUM";

        /** 困难。 */
        public static final String HARD = "HARD";

        /** 请求参数允许的难度。 */
        public static final String VALID_PATTERN = EASY + "|" + MEDIUM + "|" + HARD;

        private Difficulty() {}
    }
}
