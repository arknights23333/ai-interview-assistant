package com.assistant.aiinterviewassistant.modules.resume.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 更新简历回收站目标状态的请求参数。 */
@Data
public class UpdateResumeRecycleStatusDto {

    /** true 恢复简历，false 移入回收站，沿用现有接口的状态含义。 */
    @NotNull(message = "回收站目标状态不能为空")
    private Boolean recycleStatus;
}
