package com.assistant.aiinterviewassistant.modules.questionbank.domain.vo;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/** 题库列表中的题目摘要。 */
@Getter
@Setter
public class QuestionBankQuestionListVo {

    /** 题目 ID。 */
    private Long id;

    /** 所属知识库 ID。 */
    private Long knowledgeBaseId;

    /** 来源文档 ID，手动录题时可为空。 */
    @Nullable
    private Long sourceDocumentId;

    /** 修改草稿关联的原题 ID，正式题目为空。 */
    @Nullable
    private Long originalQuestionId;

    /** 题目最初的创建方式。 */
    private String creationType;

    /** 题目状态。 */
    private String status;

    /** 主问题题干。 */
    private String question;

    /** 岗位方向，修改草稿中可为空。 */
    @Nullable
    private String jobDirection;

    /** 题目难度，修改草稿中可为空。 */
    @Nullable
    private String difficulty;

    /** 考察分类，可为空。 */
    @Nullable
    private String category;

    /** 创建时间。 */
    private OffsetDateTime createdAt;

    /** 最近修改时间。 */
    private OffsetDateTime updatedAt;
}
