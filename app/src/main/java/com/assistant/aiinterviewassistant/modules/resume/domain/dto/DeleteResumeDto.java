package com.assistant.aiinterviewassistant.modules.resume.domain.dto;

import java.util.List;
import lombok.Data;

/** 删除当前用户回收站中的指定简历，或清空整个回收站。 */
@Data
public class DeleteResumeDto {

    /** 指定删除的简历 ID；单个删除也使用列表。 */
    private List<Long> resumeIds;

    /** 是否清空回收站，默认 false；为 true 时不应同时传入非空 ID 列表。 */
    private boolean deleteAll;
}
