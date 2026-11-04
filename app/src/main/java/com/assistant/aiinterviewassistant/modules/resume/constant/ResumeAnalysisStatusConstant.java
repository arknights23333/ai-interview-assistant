package com.assistant.aiinterviewassistant.modules.resume.constant;

/** 简历 AI 分析状态，对应 resume_analysis.status。 */
public final class ResumeAnalysisStatusConstant {

    /** 分析记录已创建，等待执行。 */
    public static final String PENDING = "PENDING";

    /** 正在分析。 */
    public static final String PROCESSING = "PROCESSING";

    /** 分析成功，报告已保存。 */
    public static final String SUCCESS = "SUCCESS";

    /** 分析失败，失败原因由 error_message 保存。 */
    public static final String FAILED = "FAILED";

    private ResumeAnalysisStatusConstant() {}
}
