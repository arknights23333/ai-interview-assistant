package com.assistant.aiinterviewassistant.infrastructure.document.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * 文本清洗服务，移除匹配规则的噪声并规范文本格式。
 */
@Service
public class TextCleaningService {

    /** HTTP、HTTPS 或 www. 开头的网址，不覆盖所有合法 URL。 */
    private static final Pattern URL = Pattern.compile(
            "(?:https?://|www\\.)[^\\s<>\"\\u4e00-\\u9fff]+",
            Pattern.CASE_INSENSITIVE
    );

    /** 整行匹配常见图片、音视频文件名，允许行尾空格和制表符。 */
    private static final Pattern IMAGE = Pattern.compile(
            "^[^/\\\\\\r\\n]+\\.(png|jpg|jpeg|webp|gif|mp3|mp4)[ \\t]*$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    /**
     * 匹配 file: 开头、以常见图片或文档扩展名结束的文件 URI，
     * 用于清理正文中可能出现的临时资源路径。
     *
     * <p>允许路径包含中文，避免将扩展名后紧贴的中文正文一起匹配。
     * 这是简化匹配规则，不支持所有合法的文件 URI。</p>
     */
    private static final Pattern FILE_URL = Pattern.compile(
            "file:[^\\s<>\"]+?\\.(?:png|jpg|jpeg|gif|bmp|webp|pdf|doc|docx)(?![a-zA-Z0-9])",
            Pattern.CASE_INSENSITIVE
    );

    /** 整行匹配至少三个分隔符，前后只允许行内空格或制表符。 */
    private static final Pattern SEPARATOR = Pattern.compile(
            "(?m)^[ \\t]*[-_=*~]{3,}[ \\t]*$"
    );

    /** 清除部分控制字符（含换页符），保留换行、回车及制表符。 */
    private static final Pattern CONTROL_CHARS = Pattern.compile(
            "[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]"
    );

    /**
     * 清理文档解析后的文本，移除匹配规则的资源名称、文件 URI、
     * 网页链接、独立分隔线及部分控制字符，并规范换行和空白。
     *
     * <p>保留正文中的段落结构，不按 HTML 标签规则删除尖括号内容。
     * 资源过滤基于正则匹配，无法判断内容来源，可能误删符合规则的原文。</p>
     *
     * @param text 解析得到的原始文本
     * @return 清理后的文本；输入为 null 或空白时返回空字符串
     */
    public String cleanText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        // 先统一换行，再执行逐行匹配及内容过滤。
        String t = text.replace("\r\n", "\n").replace("\r", "\n");
        t = CONTROL_CHARS.matcher(t).replaceAll("");
        t = URL.matcher(t).replaceAll("");
        t = FILE_URL.matcher(t).replaceAll("");
        t = IMAGE.matcher(t).replaceAll("");
        t = SEPARATOR.matcher(t).replaceAll("");

        // 去掉行尾空格和制表符，保留段落换行。
        t = t.replaceAll("(?m)[ \t]+$", "");
        // 最多保留两个连续换行符，即一个空行。
        t = t.replaceAll("\n{3,}", "\n\n");
        return t.strip();
    }

    /**
     * 清理文本并限制最大长度，超出的部分直接截断。
     *
     * @param text 原始文本
     * @param maxLength 最大长度，按 Java UTF-16 字符单位计，必须大于0
     * @return 清理后的文本（可能被截断）
     */
    public String cleanTextWithLimit(String text, int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("文本长度上限必须大于0");
        }
        String cleaned = cleanText(text);
        if (cleaned.length() > maxLength) {
            return cleaned.substring(0, maxLength);
        }
        return cleaned;
    }
}
