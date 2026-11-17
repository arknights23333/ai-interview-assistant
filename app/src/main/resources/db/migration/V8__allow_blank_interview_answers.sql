-- 允许提交白卷，仍以提交时间区分未作答和已提交空答案。
ALTER TABLE interview_session_question
    DROP CONSTRAINT ck_interview_session_question_answer;

ALTER TABLE interview_session_question
    ADD CONSTRAINT ck_interview_session_question_answer CHECK (
        (answered_at IS NULL AND user_answer IS NULL)
        OR (answered_at IS NOT NULL AND answered_at >= created_at
            AND user_answer IS NOT NULL)
    );
