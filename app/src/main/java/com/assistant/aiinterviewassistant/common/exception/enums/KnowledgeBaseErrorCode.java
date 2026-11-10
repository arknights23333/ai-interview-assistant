package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 知识库业务错误。 */
@Getter
@RequiredArgsConstructor
public enum KnowledgeBaseErrorCode implements BusinessErrorCode {

    KNOWLEDGE_BASE_NOT_FOUND(40404, HttpStatus.NOT_FOUND, "知识库不存在"),
    KNOWLEDGE_BASE_CREATE_FAILED(50022, HttpStatus.INTERNAL_SERVER_ERROR, "知识库创建失败，请稍后重试"),
    KNOWLEDGE_BASE_UPDATE_FAILED(50023, HttpStatus.INTERNAL_SERVER_ERROR, "知识库更新失败，请稍后重试"),
    KNOWLEDGE_BASE_DELETE_FAILED(50024, HttpStatus.INTERNAL_SERVER_ERROR, "知识库删除失败，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
