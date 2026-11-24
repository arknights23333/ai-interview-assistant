package com.assistant.aiinterviewassistant.modules.questionbank.domain.dto;

import com.assistant.aiinterviewassistant.modules.questionbank.constant.QuestionBankConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** 创建题库题目所需的内容。 */
@Getter
@Setter
public class CreateQuestionDto {

    /** 所属知识库 ID。 */
    @NotNull(message = "知识库 ID 不能为空")
    @Positive(message = "知识库 ID 必须为正整数")
    private Long knowledgeBaseId;

    /** 同库来源文档 ID，手动录题时可省略。 */
    @Nullable
    @Positive(message = "来源文档 ID 必须为正整数")
    private Long sourceDocumentId;

    /** 主问题题干。 */
    @NotBlank(message = "题干不能为空")
    private String question;

    /** 适用的岗位方向。 */
    @NotBlank(message = "岗位方向不能为空")
    @Size(max = 64, message = "岗位方向不能超过 64 个字符")
    private String jobDirection;

    /** 题目难度，取值为 EASY、MEDIUM、HARD。 */
    @NotBlank(message = "题目难度不能为空")
    @Pattern(regexp = QuestionBankConstant.Difficulty.VALID_PATTERN, message = "题目难度必须为 EASY、MEDIUM 或 HARD")
    private String difficulty;

    /** 考察分类，可为空。 */
    @Nullable
    @Size(max = 64, message = "考察分类不能超过 64 个字符")
    private String category;

    /** 参考答案。 */
    @NotBlank(message = "参考答案不能为空")
    private String referenceAnswer;

    /** 评分要点，至少填写一项。 */
    @NotEmpty(message = "至少填写一个评分要点")
    private List<@NotBlank(message = "评分要点不能包含空白项") String> keyPoints;

    /** 单题评分标准，可为空。 */
    @Nullable
    private String scoringRubric;
}
