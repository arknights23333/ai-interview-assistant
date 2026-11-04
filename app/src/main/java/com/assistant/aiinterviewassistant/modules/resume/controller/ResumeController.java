package com.assistant.aiinterviewassistant.modules.resume.controller;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadVo;
import com.assistant.aiinterviewassistant.modules.resume.service.ResumeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
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


    /**
     * 提交当前用户的简历正文解析任务，成功响应不代表解析已完成。
     *
     * @param jwt 已通过认证的登录身份
     * @param resumeId 简历 ID
     * @return 解析任务提交结果
     */
    @PostMapping("/{resumeId}/parse")
    public R<Void> parseResume(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("resumeId") Long resumeId) {
        return resumeService.parseResume(resumeId, Long.valueOf(jwt.getSubject()));
    }
}
