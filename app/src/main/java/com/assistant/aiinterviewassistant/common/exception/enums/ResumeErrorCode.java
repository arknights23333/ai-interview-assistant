package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 简历业务错误码，文件读写和通用解析错误使用 FileErrorCode。 */
@Getter
@RequiredArgsConstructor
public enum ResumeErrorCode implements BusinessErrorCode {

    /** 当前用户可访问的有效简历不存在，包含不属于当前用户或已移入回收站。 */
    RESUME_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "简历不存在或已被删除"),

    /** 简历正文解析任务已在排队或执行，不应重复提交。 */
    PARSE_IN_PROGRESS(40904, HttpStatus.CONFLICT, "简历正在解析，请勿重复提交"),

    /** 依赖正文的后续操作要求解析成功，当前尚未满足。 */
    RESUME_NOT_PARSED(40905, HttpStatus.CONFLICT, "简历尚未解析成功，请先完成正文解析"),

    /** 条件更新未生效，简历状态可能已被其他请求修改。 */
    RESUME_STATE_CHANGED(40906, HttpStatus.CONFLICT, "简历状态已变化，请刷新后重试"),

    /** 文件可读取，但解析并清洗后没有有效正文，例如未进行 OCR 的扫描件。 */
    RESUME_CONTENT_EMPTY(42201, HttpStatus.UNPROCESSABLE_ENTITY, "未提取到有效正文，请检查文件是否为扫描件或内容为空"),

    /** 解析执行器无法接受新任务，例如线程池队列已满。 */
    PARSE_TASK_BUSY(50301, HttpStatus.SERVICE_UNAVAILABLE, "解析任务繁忙，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
