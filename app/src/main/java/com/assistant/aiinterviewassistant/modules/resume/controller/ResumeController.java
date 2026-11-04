package com.assistant.aiinterviewassistant.modules.resume.controller;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadVo;
import com.assistant.aiinterviewassistant.modules.resume.service.ResumeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    @PostMapping("/upload")
    public R<ResumeUploadVo> upload(@AuthenticationPrincipal Jwt jwt, MultipartFile file) {
        return resumeService.upload(Long.valueOf(jwt.getSubject()), file);
    }

}
