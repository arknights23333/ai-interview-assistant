package com.assistant.aiinterviewassistant.modules.resume.domain.vo;

/**
 * 批量上传简历结果
 * @param index 文件索引，这样可以区别同名文件
 * @param filename 文件名
 * @param resumeId 简历id
 * @param status 状态
 * @param errorCode 错误码
 * @param message 错误信息
 */
public record ResumeUploadBatchVo(
        int index,
        String filename,
        Long resumeId,
        Status status,
        String errorCode,
        String message

) {
    public enum Status{
        SUCCESS,//成功
        FAILED,//失败
        REUSED//复用
    }
}
