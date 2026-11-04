package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 文件操作的业务错误定义，供简历、知识库和头像等上传场景复用。
 */
@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements BusinessErrorCode {

    /** 没有选择文件，或者文件内容为空。 */
    FILE_EMPTY(40010, HttpStatus.BAD_REQUEST, "上传文件不能为空"),

    /** 文件超过对应业务允许的大小。 */
    FILE_TOO_LARGE(41301, HttpStatus.PAYLOAD_TOO_LARGE, "文件大小超过限制"),

    /** 文件实际类型不在对应业务允许的范围内。 */
    FILE_TYPE_NOT_SUPPORTED(41501, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "不支持的文件类型"),

    /** 服务端读取上传文件内容失败。 */
    FILE_READ_FAILED(50007, HttpStatus.INTERNAL_SERVER_ERROR, "文件读取失败，请重新上传"),

    /** 将文件写入对象存储失败。 */
    FILE_UPLOAD_FAILED(50008, HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败，请稍后重试"),

    /** 删除对象存储中的文件失败。 */
    FILE_DELETE_FAILED(50009, HttpStatus.INTERNAL_SERVER_ERROR, "文件删除失败，请稍后重试"),

    /** 文件类型检测过程中读取失败，无法完成检测。 */
    FILE_TYPE_DETECTION_FAILED(50010, HttpStatus.INTERNAL_SERVER_ERROR, "文件类型检测失败，请重新上传"),

    /** 计算文件内容的 SHA-256 哈希失败。 */
    FILE_HASH_FAILED(50011, HttpStatus.INTERNAL_SERVER_ERROR, "文件哈希计算失败，请稍后重试"),

    /** 文档解析失败，无法提取正文。 */
    FILE_PARSE_ERROR(50012, HttpStatus.INTERNAL_SERVER_ERROR, "文件解析失败，请检查文件是否损坏或更换文件"),

    /** 对象存储中的文件不存在。 */
    FILE_NOT_FOUND(40410, HttpStatus.NOT_FOUND, "文件不存在或已被删除"),
    /** 从对象存储读取文件内容失败。 */
    FILE_DOWNLOAD_FAILED(50013, HttpStatus.INTERNAL_SERVER_ERROR, "文件下载失败，请稍后重试");
    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
