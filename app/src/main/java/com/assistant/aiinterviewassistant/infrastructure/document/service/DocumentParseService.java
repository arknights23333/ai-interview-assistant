package com.assistant.aiinterviewassistant.infrastructure.document.service;


import com.assistant.aiinterviewassistant.common.exception.enums.FileErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.infrastructure.document.NoOpEmbeddedDocumentExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * 解析文件原始文本服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParseService {


    private static final int MAX_TEXT_LENGTH=5*1024*1024;
    /**
     * 解析上传的文件，提取文本内容
     *
     * @param file 上传的文件（支持PDF、DOCX、DOC、TXT、MD等）
     * @return 提取的文本内容
     */
    public String parseContent(MultipartFile file) throws IOException, TikaException, SAXException {
        String fileName = file.getOriginalFilename();
        log.info("开始解析文件: {}", fileName);
        if (file.isEmpty() || file.getSize() == 0) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
        try(InputStream inputStream = file.getInputStream()){
            return parseContent(inputStream);
        }

    }

    /**
     * 核心解析方法
     * @param inputStream 输入流
     * @return解析出来的正文
     * @throws TikaException
     * @throws IOException
     * @throws SAXException
     */
    private String parseContent(InputStream inputStream) throws TikaException, IOException, SAXException {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler=new BodyContentHandler(MAX_TEXT_LENGTH);
        Metadata metadata =new Metadata();
        ParseContext context =new ParseContext();
        context.set(Parser.class, parser);
        //不处理嵌入式资源
        context.set(EmbeddedDocumentExtractor.class, new NoOpEmbeddedDocumentExtractor());
        //配置PDF解析参数
        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(false);
        pdfConfig.setSortByPosition(true); // 按 x/y 坐标排序文本，改善多栏布局解析顺序
        context.set(PDFParserConfig.class, pdfConfig);

        parser.parse(inputStream, handler, metadata);
        return handler.toString();

    }
}
