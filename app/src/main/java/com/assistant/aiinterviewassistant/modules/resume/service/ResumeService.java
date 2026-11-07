package com.assistant.aiinterviewassistant.modules.resume.service;

import java.util.List;
import com.assistant.aiinterviewassistant.modules.resume.domain.dto.DeleteResumeDto;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.resume.domain.po.ResumePo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeParseBatchVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadBatchVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeUploadVo;
import com.assistant.aiinterviewassistant.modules.resume.domain.vo.ResumeVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历服务
 */
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

    /** 批量提交正文解析任务，按去重后的请求顺序返回逐项结果。 */
    R<List<ResumeParseBatchVo>> parseResumeBatch(List<Long> resumeIds, Long userId);

    /**
     * 查询简历
     * @param resumeId
     * @param userId
     * @return
     */
    R<ResumeVo> getResume(Long resumeId, Long userId);

    /** 分页查询当前用户的正常简历或回收站简历。 */
    R<Page<ResumeVo>> getResumeList(Long userId, PageQuery pageQuery, boolean recycled);

    /** 更新单份简历的回收站状态，true 恢复，false 移入回收站。 */
    R<Void> updateRecycleStatus(Long resumeId, boolean recycleStatus, Long userId);

    /** 批量更新回收站状态，返回本次未达到目标状态的简历 ID。 */
    R<List<Long>> updateRecycleStatusBatch(List<Long> resumeIds, boolean recycleStatus, Long userId);

    /**
     * 删除当前用户回收站中的简历。
     *
     * @param dto 删除范围，指定 ID 与全部删除模式不能同时使用
     * @param userId 当前登录用户 ID
     * @return 本次未被数据库删除的简历 ID
     */
    R<List<Long>> deleteResume(DeleteResumeDto dto, Long userId);

    /**
     * 批量上传简历
     * @param userId 用户id
     * @param files 简历文件列表
     * @return
     */
    R<List<ResumeUploadBatchVo>> uploadBatch(Long userId, List<MultipartFile> files);

    /**
     * 下载简历，返回预签名下载链接
     * @param resumeId
     * @param aLong
     * @return
     */
    R<String> download(Long resumeId, Long aLong);
}
