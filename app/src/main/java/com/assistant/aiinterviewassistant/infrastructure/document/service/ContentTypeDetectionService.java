package com.assistant.aiinterviewassistant.infrastructure.document.service;

import com.assistant.aiinterviewassistant.common.exception.enums.FileErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.Tika;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件类型检测服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentTypeDetectionService {

    private final Tika tika;

    /**
     * 检测文件类型
     * @param file
     * @return 检测出的文件类型
     */
    public String detectContentType(MultipartFile file)  {
        try(InputStream stream=file.getInputStream()){
            return tika.detect(stream, file.getOriginalFilename());
        }catch (IOException e){
            log.error("文件类型检测失败: filename={},错误信息：{}", file.getOriginalFilename(), e.getMessage(),e);
            throw new BusinessException(FileErrorCode.FILE_TYPE_DETECTION_FAILED, e);

        }
    }

}
