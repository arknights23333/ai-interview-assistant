package com.assistant.aiinterviewassistant.modules.resume.controller;
import com.assistant.aiinterviewassistant.modules.resume.domain.dto.BatchUpdateResumeRecycleStatusDto;
import com.assistant.aiinterviewassistant.modules.resume.domain.dto.DeleteResumeDto;
import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.resume.domain.dto.UpdateResumeRecycleStatusDto;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeParseBatchVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadBatchVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeVo;
import com.assistant.aiinterviewassistant.modules.resume.service.ResumeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 简历模块接口
 * @folder 简历模块
 */
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * 获取简历信息
     * @param resumeId
     * @param jwt
     * @return
     */
    @GetMapping("/get/{resumeId}")
    public R<ResumeVo> getResume(@PathVariable("resumeId") Long resumeId, @AuthenticationPrincipal Jwt jwt){
        return resumeService.getResume(resumeId, Long.valueOf(jwt.getSubject()));
    }

    /**
     * 获取用户的简历列表
     * @param
     * @return
     */
    @PostMapping("/list")
    public R<Page<ResumeVo>> getResumeList(@AuthenticationPrincipal Jwt jwt, @Valid @ModelAttribute PageQuery pageQuery) {
        return resumeService.getResumeList(Long.valueOf(jwt.getSubject()),pageQuery);
    }

    /**
     * 上传简历
     * @param jwt
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<ResumeUploadVo> upload(@AuthenticationPrincipal Jwt jwt, MultipartFile file) {
        return resumeService.upload(Long.valueOf(jwt.getSubject()), file);
    }

    /**
     * 批量上传简历
     * @param jwt
     * @param files
     * @return
     */
    @PostMapping("/uploadBatch")
    public R<List<ResumeUploadBatchVo>> uploadBatch(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return resumeService.uploadBatch(Long.valueOf(jwt.getSubject()), files);
    }

    /**
     * 提交当前用户的简历正文解析任务，成功响应不代表解析已完成。
     * @param jwt 已通过认证的登录身份
     * @param resumeId 简历 ID
     * @return 解析任务提交结果
     */
    @PostMapping("/parse/{resumeId}")
    public R<Void> parseResume(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("resumeId") Long resumeId) {
        return resumeService.parseResume(resumeId, Long.valueOf(jwt.getSubject()));
    }

    /**
     * 批量提交当前用户的简历正文解析任务，成功响应不代表解析已完成。
     * @param jwt
     * @param resumeIds
     * @return
     */
    @PostMapping("/parseBatch")
    public R<List<ResumeParseBatchVo>> parseBatch(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody List<Long> resumeIds){
        return resumeService.parseResumeBatch(resumeIds, Long.valueOf(jwt.getSubject()));
    }
    /**
     * 更新简历回收站状态。
     *
     * @param resumeId 简历 ID
     * @param dto 目标状态：true 恢复，false 移入回收站
     * @param jwt 当前登录身份
     * @return 状态更新结果
     */
    @PutMapping("/recycle-status/{resumeId}")
    public R<Void> updateRecycleStatus(
            @PathVariable("resumeId") Long resumeId,
            @Valid @RequestBody UpdateResumeRecycleStatusDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        return resumeService.updateRecycleStatus(
                resumeId, dto.getRecycleStatus(), Long.valueOf(jwt.getSubject()));
    }

    /**
     * 批量更新简历回收站状态。
     *
     * @param dto 简历 ID 列表和目标状态
     * @param jwt 当前登录身份
     * @return 本次未达到目标状态的简历 ID
     */
    @PutMapping("/recycle-status")
    public R<List<Long>> updateRecycleStatusBatch(
            @Valid @RequestBody BatchUpdateResumeRecycleStatusDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        return resumeService.updateRecycleStatusBatch(
                dto.getResumeIds(), dto.getRecycleStatus(), Long.valueOf(jwt.getSubject()));
    }

    /**
     * 删除回收站中的单个、多个或全部简历。
     *
     * @param dto 删除范围
     * @param jwt 当前登录身份
     * @return 本次未被数据库删除的简历 ID
     */
    @DeleteMapping("/delete")
    public R<List<Long>> deleteResume(
            @Valid @RequestBody DeleteResumeDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        return resumeService.deleteResume(dto, Long.valueOf(jwt.getSubject()));
    }
}
