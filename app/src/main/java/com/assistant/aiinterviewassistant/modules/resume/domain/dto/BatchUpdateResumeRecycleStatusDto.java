package com.assistant.aiinterviewassistant.modules.resume.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 批量更新简历回收站状态的请求参数。 */
@Getter
@Setter
public class BatchUpdateResumeRecycleStatusDto extends UpdateResumeRecycleStatusDto {

    /** 要更新状态的简历 ID。 */
    @NotEmpty(message = "请选择要更新的简历")
    private List<@NotNull(message = "简历 ID 不能为空") Long> resumeIds;
}
