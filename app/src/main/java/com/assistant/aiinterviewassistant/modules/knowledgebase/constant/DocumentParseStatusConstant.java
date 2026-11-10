package com.assistant.aiinterviewassistant.modules.knowledgebase.constant;

/** 知识库文档处理状态，对应 document.process_status。 */
public final class DocumentParseStatusConstant {

    /** 等待处理。 */
    public static final String PENDING = "PENDING";

    /** 正在处理。 */
    public static final String PROCESSING = "PROCESSING";

    /** 处理完成。 */
    public static final String COMPLETED = "COMPLETED";

    /** 处理失败，失败原因由 process_error 保存。 */
    public static final String FAILED = "FAILED";

    private DocumentParseStatusConstant() {}
}
