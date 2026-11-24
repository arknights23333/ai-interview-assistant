package com.assistant.aiinterviewassistant.modules.questionbank.domain.dto;

import com.assistant.aiinterviewassistant.modules.questionbank.constant.QuestionBankConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/** 按知识库分页查询题目的筛选条件。 */
@Getter
@Setter
public class QuestionBankQueryDto {

    /** 所属知识库 ID。 */
    @NotNull(message = "知识库 ID 不能为空")
    @Positive(message = "知识库 ID 必须为正整数")
    private Long knowledgeBaseId;

    /** 岗位方向关键词，用于模糊搜索。 */
    @Nullable
    @Size(max = 64, message = "岗位方向不能超过 64 个字符")
    private String jobDirection;

    /** 题目难度，可选 EASY、MEDIUM、HARD。 */
    @Nullable
    @Pattern(regexp = QuestionBankConstant.Difficulty.VALID_PATTERN, message = "题目难度必须为 EASY、MEDIUM 或 HARD")
    private String difficulty;

    /** 题目状态，可选 DRAFT、ACTIVE、ARCHIVED，省略时查询所有状态。 */
    @Nullable
    @Pattern(regexp = QuestionBankConstant.Status.VALID_PATTERN, message = "题目状态必须为 DRAFT、ACTIVE 或 ARCHIVED")
    private String status;
}
