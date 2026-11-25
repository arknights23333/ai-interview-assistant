package com.assistant.aiinterviewassistant.modules.questionbank.constant;

/** 题库模块的状态、创建方式和难度。 */
public final class QuestionBankConstant {

    private QuestionBankConstant() {}

    /** 题目状态，对应 question_bank_question.status。 */
    public static final class Status {

        /** 已启用题目的修改草稿。 */
        public static final String DRAFT = "DRAFT";

        /** 已启用，可用于专项面试。 */
        public static final String ACTIVE = "ACTIVE";

        /** 已归档。 */
        public static final String ARCHIVED = "ARCHIVED";

        /** 列表筛选允许的状态。 */
        public static final String VALID_PATTERN = DRAFT + "|" + ACTIVE + "|" + ARCHIVED;

        private Status() {}
    }

    /** 题目最初的创建方式，对应 question_bank_question.creation_type。 */
    public static final class CreationType {

        /** 用户手动录入。 */
        public static final String MANUAL = "MANUAL";

        /** 基于来源文档由 AI 生成。 */
        public static final String AI_GENERATED = "AI_GENERATED";

        private CreationType() {}
    }

    /** 创建草稿时由后端填充的内容。 */
    public static final class DraftPlaceholder {

        /** 题干被显式置空时写入的占位文本，满足题干非空白约束。 */
        public static final String QUESTION = "待补充题干";

        /** 评分要点被显式清空时写入的空数组。 */
        public static final String EMPTY_KEY_POINTS_JSON = "[]";

        private DraftPlaceholder() {}
    }

    /** 题库题目的难度。 */
    public static final class Difficulty {

        /** 简单。 */
        public static final String EASY = "EASY";

        /** 中等。 */
        public static final String MEDIUM = "MEDIUM";

        /** 困难。 */
        public static final String HARD = "HARD";

        /** 请求字段允许的难度取值。 */
        public static final String VALID_PATTERN = EASY + "|" + MEDIUM + "|" + HARD;

        private Difficulty() {}
    }
}
