package com.assistant.aiinterviewassistant.infrastructure.storage.domain;

import java.util.List;

/**
 * 对象存储批量删除的数据模型
 */
public final class StorageDeleteModels {
    private StorageDeleteModels(){}

    /** 批量删除结果中使用的项目失败类型。 */
    public enum DeleteFailureCode {
        INVALID_OBJECT_KEY,
        INVALID_BUSINESS_ID,
        DELETE_RESULT_UNKNOWN,
        S3_DELETE_FAILED
    }

    /**
     * 删除失败或结果未确认的对象。
     *
     * @param key 对象键
     * @param id 对应的业务 ID
     * @param code 项目定义的失败类型
     * @param s3Code S3 逐项响应的原始错误码，未收到逐项错误时为空
     * @param errorMessage 失败详情
     * @param <T> 业务 ID 类型
     */
    public record DeleteFailure<T>(
            String key,
            T id,
            DeleteFailureCode code,
            String s3Code,
            String errorMessage
    ) {}

    /**
     * 返回结果
     */
    public record BatchDeleteResult<T>(
            List<T> successIds,
            List<DeleteFailure<T>> failures
    ){}



}
