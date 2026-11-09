package com.assistant.aiinterviewassistant.modules.resume.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

/** 一份简历的解析任务提交结果。 */
public record ResumeParseBatchVo(
        @Nullable Long resumeId,
        ParseSubmitOutcome status,
        @Nullable String errorCode,
        @Nullable String message
) {

    @Getter
    @RequiredArgsConstructor
    public enum ParseSubmitOutcome {
        SUBMITTED("解析任务已提交"),
        ALREADY_PROCESSING("解析任务正在处理，请勿重复提交"),
        ALREADY_PARSED("解析任务已完成，请勿重复提交"),
        FAILED("解析任务提交失败");

        private final String message;
    }
}
