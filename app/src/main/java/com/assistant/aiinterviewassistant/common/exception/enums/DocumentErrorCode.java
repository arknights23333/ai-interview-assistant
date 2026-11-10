package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 知识库文档业务错误。 */
@Getter
@RequiredArgsConstructor
public enum DocumentErrorCode implements BusinessErrorCode {

    INVALID_DOCUMENT_IDS(40018, HttpStatus.BAD_REQUEST, "文档 ID 不能为空且必须为正整数"),
    DOCUMENT_NOT_FOUND(40405, HttpStatus.NOT_FOUND, "文档不存在"),
    DOCUMENT_SAVE_FAILED(50025, HttpStatus.INTERNAL_SERVER_ERROR, "文档记录保存失败，请稍后重试"),
    DOCUMENT_DELETE_FAILED(50026, HttpStatus.INTERNAL_SERVER_ERROR, "文档记录删除失败，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
