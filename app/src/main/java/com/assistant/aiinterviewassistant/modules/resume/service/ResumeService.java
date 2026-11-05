package com.assistant.aiinterviewassistant.modules.resume.service;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.resume.domain.po.ResumePo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 校验简历归属并提交后台正文解析任务。
     *
     * @param resumeId 简历 ID
     * @param userId 当前登录用户 ID
     * @return 任务提交结果，最终解析状态通过后续查询获取
     */
    R<Void> parseResume(Long resumeId, Long userId);

    /**
     * 查询简历
     * @param resumeId
     * @param userId
     * @return
     */
    R<ResumeVo> getResume(Long resumeId, Long userId);

    /**
     * 查询用户的简历列表
     * @param userId
     * @return
     */
    R<Page<ResumeVo>> getResumeList(Long userId, PageQuery pageQuery);

    /**
     * 更新简历回收站状态
     *
     * @param resumeId
     * @param recycleStatus true 恢复，false 移入回收站
     * @param userId 当前登录用户 ID
     * @return
     */
    R<Void> updateRecycleStatus(Long resumeId, Boolean recycleStatus, Long userId);
}
