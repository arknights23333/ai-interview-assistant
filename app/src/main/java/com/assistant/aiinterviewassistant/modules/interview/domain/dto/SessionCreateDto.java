package com.assistant.aiinterviewassistant.modules.interview.domain.dto;

import com.assistant.aiinterviewassistant.modules.interview.constant.InterviewSourceTypeConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** 创建文字面试的请求参数，普通面试和知识库专项共用。 */
@Data
public class SessionCreateDto {

    /** 题目来源：NORMAL 普通面试，KNOWLEDGE_BASE 知识库专项。 */
    @NotBlank(message = "面试题目来源不能为空")
    @Pattern(regexp = InterviewSourceTypeConstant.VALID_PATTERN, message = "面试题目来源不合法")
    private String sourceType;

    /** 岗位方向或专项题库方向。 */
    @NotBlank(message = "面试方向不能为空")
    @Size(max = 64, message = "面试方向不能超过64个字符")
    private String jobDirection;

    /** 本场面试的难度标识。 */
    @NotBlank(message = "面试难度不能为空")
    @Size(max = 16, message = "面试难度不能超过16个字符")
    private String difficulty;

    /** 关联的本人简历 ID，可不传。 */
    @Nullable
    @Positive(message = "简历 ID 必须为正整数")
    private Long resumeId;

    /** 专项面试的本人知识库 ID，专项时是否必填及资源归属由会话服务校验。 */
    @Nullable
    @Positive(message = "知识库 ID 必须为正整数")
    private Long knowledgeBaseId;

    /** 用户提供的岗位描述，可不传。 */
    @Nullable
    private String jobDescription;

    /** 按出题顺序排列的主问题草稿，题目服务按列表顺序生成轮次。 */
    @NotEmpty(message = "主问题列表不能为空")
    private List<@NotNull(message = "主问题不能为 null") @Valid MainQuestionDraft> questions;
}
