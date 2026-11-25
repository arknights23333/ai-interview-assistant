package com.assistant.aiinterviewassistant.modules.questionbank.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 创建题目草稿的入参。
 *
 * <p>字段传 null 表示沿用原题，传空字符串表示显式置空，传非空值表示覆盖。
 * keyPointsJson 传 null 沿用原题，传空字符串写入 []，传 JSON 字符串数组则覆盖。
 * creationType 和 status 由服务端从原题继承或显式写入 DRAFT，前端不传。</p>
 */
@Getter
@Setter
public class CreateDraftDto {

    /** 修改草稿对应的原题 ID，必填。 */
    private Long originalQuestionId;

    /** 同库来源文档 ID；为空时沿用原题来源文档。 */
    private Long sourceDocumentId;

    /** 主问题题干；为空字符串时写入占位符。 */
    private String question;

    /** 适用的岗位方向；为空字符串时置空。 */
    private String jobDirection;

    /** 题目难度；为空字符串时置空，非空必须为 EASY、MEDIUM 或 HARD。 */
    private String difficulty;

    /** 考察分类；为空字符串时置空。 */
    private String category;

    /** 参考答案；为空字符串时置空。 */
    private String referenceAnswer;

    /** 评分要点 JSON 字符串数组；为空字符串时写入 []。 */
    private String keyPointsJson;

    /** 单题评分标准；为空字符串时置空。 */
    private String scoringRubric;
}
