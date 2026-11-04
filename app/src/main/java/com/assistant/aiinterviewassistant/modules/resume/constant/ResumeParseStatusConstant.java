package com.assistant.aiinterviewassistant.modules.resume.constant;

/** 简历正文解析状态，对应 resume.parse_status。 */
public final class ResumeParseStatusConstant {

    /** 尚未解析。 */
    public static final String PENDING = "PENDING";

    /** 正在解析。 */
    public static final String PROCESSING = "PROCESSING";

    /** 解析成功，正文已保存。 */
    public static final String SUCCESS = "SUCCESS";

    /** 解析失败，失败原因由 parse_error 保存。 */
    public static final String FAILED = "FAILED";

    private ResumeParseStatusConstant() {}
}
