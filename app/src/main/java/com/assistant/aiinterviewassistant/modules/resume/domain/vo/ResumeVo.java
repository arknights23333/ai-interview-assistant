package com.assistant.aiinterviewassistant.modules.resume.domain.vo;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ResumeVo {
    private Long id;

    private OffsetDateTime createAt;

    private String resumeContent;

    private String contentType;

    private Long fileSize;

    private String fileName;

    private String parseStatus;

    private String parseError;

}
