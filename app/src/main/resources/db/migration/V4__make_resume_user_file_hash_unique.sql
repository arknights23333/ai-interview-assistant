-- 同一用户、同一文件哈希始终只保留一条简历记录，包含回收站记录。
-- 重复上传时复用已有记录；已有记录在回收站时恢复，不再创建第二条记录。
CREATE UNIQUE INDEX uk_resume_user_file_hash
    ON resume (user_id, file_hash);

-- 新索引同时覆盖有效记录与回收站记录，替代原先仅约束有效记录的部分唯一索引。
DROP INDEX uk_resume_user_file_hash_active;

COMMENT ON INDEX uk_resume_user_file_hash IS
    '同一用户的文件内容唯一，不区分是否已移入回收站';
