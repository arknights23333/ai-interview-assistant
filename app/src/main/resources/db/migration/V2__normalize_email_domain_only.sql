-- 邮箱本地部分保留大小写，仅规范化最后一个 @ 后的域名。
-- 不修改已执行的 V1，避免 Flyway 校验和变化。
UPDATE app_user
SET email = LEFT(email, LENGTH(email) - STRPOS(REVERSE(email), '@'))
    || LOWER(RIGHT(email, STRPOS(REVERSE(email), '@')));

-- 替换原先对整个地址 LOWER(email) 的唯一索引。
-- 表达式索引让直接 SQL 写入的域名大小写变体同样受唯一性约束。
DROP INDEX uk_app_user_email;
CREATE UNIQUE INDEX uk_app_user_email ON app_user (
    (LEFT(email, LENGTH(email) - STRPOS(REVERSE(email), '@'))
        || LOWER(RIGHT(email, STRPOS(REVERSE(email), '@'))))
);

COMMENT ON COLUMN app_user.email IS '已验证邮箱，唯一；本地部分区分大小写，域名不区分大小写';
