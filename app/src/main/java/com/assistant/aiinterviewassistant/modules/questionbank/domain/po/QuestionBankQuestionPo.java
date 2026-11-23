package com.assistant.aiinterviewassistant.modules.questionbank.domain.po;

import com.assistant.aiinterviewassistant.infrastructure.persistence.typehandler.JsonbStringTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;

import java.time.OffsetDateTime;

/** 知识库题库中的原题或修改草稿，对应 question_bank_question 表。 */
@Getter
@Setter
@TableName(value = "question_bank_question", autoResultMap = true)
public class QuestionBankQuestionPo {

    /** 题库记录主键，由数据库生成。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属知识库 ID。 */
    private Long knowledgeBaseId;

    /** 同库来源文档 ID，手动录题时可为空。 */
    private Long sourceDocumentId;

    /** 修改草稿指向的原题 ID，新题草稿和正式题目为空。 */
    private Long originalQuestionId;

    /** 题目最初的创建方式。 */
    private String creationType;

    /** 题目状态，新建记录默认是草稿。 */
    private String status;

    /** 主问题题干。 */
    private String question;

    /** 适用的岗位方向。 */
    private String jobDirection;

    /** 题目难度。 */
    private String difficulty;

    /** 考察分类，可为空。 */
    private String category;

    /** 参考答案。 */
    private String referenceAnswer;

    /** 评分要点，保存为 JSON 字符串数组。 */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbStringTypeHandler.class)
    private String keyPointsJson;

    /** 单题评分标准，可为空。 */
    private String scoringRubric;

    /** 候选追问，保存为 JSON 数组。 */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbStringTypeHandler.class)
    private String followUpCandidatesJson;

    /** 记录创建时间。 */
    private OffsetDateTime createdAt;

    /** 记录最近修改时间。 */
    private OffsetDateTime updatedAt;
}
