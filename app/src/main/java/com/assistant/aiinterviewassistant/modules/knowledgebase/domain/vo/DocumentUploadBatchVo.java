package com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo;

import org.jspecify.annotations.Nullable;

/**
 * 批量上传中的单份文档结果。
 *
 * @param index 文件在本次请求中的下标，从 0 开始
 * @param filename 文件名，复用时为已有文档的文件名
 * @param documentId 新增或复用的文档 ID，上传失败时为空
 * @param status 本次上传的结果
 * @param errorCode 失败时的业务错误码
 * @param message 失败时的提示信息
 */
public record DocumentUploadBatchVo(
        int index,
        @Nullable String filename,
        @Nullable Long documentId,
        Status status,
        @Nullable String errorCode,
        @Nullable String message
) {
    public enum Status {
        SUCCESS,
        FAILED,
        REUSED
    }
}
