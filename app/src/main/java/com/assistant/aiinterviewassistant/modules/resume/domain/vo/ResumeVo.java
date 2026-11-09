package com.assistant.aiinterviewassistant.modules.resume.domain.vo;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
public class ResumeVo {
    private Long id;

    private OffsetDateTime createAt;

    @Nullable
    private String resumeContent;

    private String contentType;

    private Long fileSize;

    private String fileName;

    private String parseStatus;

    @Nullable
    private String parseError;

}
