package com.assistant.aiinterviewassistant.infrastructure.document;

import lombok.extern.slf4j.Slf4j;

import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
/**
 * 空操作的嵌入文档提取器
 * 用于禁用 Tika 对嵌入资源（图片、附件等）的解析
 *
 */
@Slf4j
public class NoOpEmbeddedDocumentExtractor implements EmbeddedDocumentExtractor {
    /**
     *
     * @param metadata
     * @return
     */
    @Override
    public boolean shouldParseEmbedded(Metadata metadata) {
        //记录跳过的文档
        String fileName = metadata.get("resourceName");
        log.debug("跳过文档的名字: {}", fileName);
        return false;
    }

    /**
     * 这个方法是滚木
     * @param stream The embedded resource
     * @param handler The handler to use
     * @param metadata The metadata for the embedded resource
     * @param outputHtml Should we output HTML for this resource, or has the parser already done so?
     * @throws SAXException
     * @throws IOException
     */
    @Override
    public void parseEmbedded(InputStream stream, ContentHandler handler, Metadata metadata, boolean outputHtml) throws SAXException, IOException {

    }
}
