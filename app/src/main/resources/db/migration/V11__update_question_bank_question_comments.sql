-- 同步题库列注释，记录正式题目与修改草稿的区别。
COMMENT ON COLUMN question_bank_question.original_question_id IS '修改草稿对应的原题 ID，正式题目为空';
COMMENT ON COLUMN question_bank_question.status IS '题目状态。应用保存新题时显式写入 ACTIVE（已启用）。DRAFT 表示修改草稿，ARCHIVED 表示已归档';
COMMENT ON COLUMN question_bank_question.question IS '主问题题干，创建题目和保存修改草稿时必填';
