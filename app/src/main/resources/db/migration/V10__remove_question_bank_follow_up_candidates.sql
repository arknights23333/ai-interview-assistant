-- 追问根据实际回答现场生成，题库题目仅维护主问题及评分材料。
ALTER TABLE question_bank_question
    DROP CONSTRAINT ck_question_bank_question_follow_ups,
    DROP COLUMN follow_up_candidates_json;
