package com.assistant.aiinterviewassistant.modules.resume.domain.vo;

import lombok.Data;
@Data
public class ResumeUploadVo {
    //简历在数据库里的主键id
    private Long id;

    public ResumeUploadVo(Long id) {
        this.id = id;
    }
}
