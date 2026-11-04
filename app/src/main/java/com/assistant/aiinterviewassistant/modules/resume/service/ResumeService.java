package com.assistant.aiinterviewassistant.modules.resume.service;

import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.resume.domain.po.ResumePo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadVo;
import com.baomidou.mybatisplus.spring.service.IService;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService extends IService<ResumePo> {
    /**
     * 上传简历
     * @param id 用户id
     * @param file 简历文件
     * @return
     */
     R<ResumeUploadVo> upload(@Nullable Long id, MultipartFile file);
}
