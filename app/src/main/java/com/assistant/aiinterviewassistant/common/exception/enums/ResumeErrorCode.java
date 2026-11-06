package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 简历业务错误码，文件读写和通用解析错误使用 FileErrorCode。 */
@Getter
@RequiredArgsConstructor
public enum ResumeErrorCode implements BusinessErrorCode {

    /** 必须指定要删除的简历，或者选择清空回收站。 */
    INVALID_DELETE_SCOPE(40011, HttpStatus.BAD_REQUEST, "请选择要删除的简历，或单独选择清空回收站"),

    /** 批量上传的简历数量超过五份。 */
    BATCH_UPLOAD_LIMIT_EXCEEDED(40012, HttpStatus.BAD_REQUEST, "一次最多上传五份简历"),

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
    PARSE_TASK_BUSY(50301, HttpStatus.SERVICE_UNAVAILABLE, "解析任务繁忙，请稍后重试"),

    /** 简历信息或回收站状态更新失败。 */
    RESUME_UPDATE_FAILED(50014, HttpStatus.INTERNAL_SERVER_ERROR, "简历更新失败，请稍后重试"),

    /** 彻底删除简历或清空回收站失败。 */
    RESUME_DELETE_FAILED(50015, HttpStatus.INTERNAL_SERVER_ERROR, "简历删除失败，请稍后重试"),

    /** 上传文件后保存简历记录失败。 */
    RESUME_SAVE_FAILED(50016, HttpStatus.INTERNAL_SERVER_ERROR, "简历保存失败，请稍后重试"),

    /** 上传前查询已有简历或恢复回收站中的简历失败。 */
    RESUME_UPLOAD_CHECK_FAILED(50017, HttpStatus.INTERNAL_SERVER_ERROR, "检查或恢复已有简历失败，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
