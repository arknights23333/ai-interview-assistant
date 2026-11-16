package com.assistant.aiinterviewassistant.modules.interview.domain.po;

import com.assistant.aiinterviewassistant.infrastructure.persistence.typehandler.JsonbStringTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;

import java.time.OffsetDateTime;

/** 本场题目的快照与作答记录，主问题和实际提出的追问各占一行。 */
@Getter
@Setter
@TableName(value = "interview_session_question", autoResultMap = true)
public class InterviewSessionQuestionPo {

    /** 本场题目主键，由数据库自增生成。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属面试会话 ID。 */
    private Long sessionId;

    /** 主问题轮次，从 1 开始。 */
    private Integer roundNo;

    /** 轮内顺序，0 表示主问题，正整数表示第几道追问。 */
    private Integer followUpNo;

    /** 被追问的本场题目 ID，主问题为空。 */
    private Long parentQuestionId;

    /** 本场实际使用的题干快照。 */
    private String question;

    /** 本场题目的考察分类，可为空。 */
    private String category;

    /** 出题时保存的参考答案。 */
    private String referenceAnswer;

    /** 出题时保存的答题要点，内容为 JSON 数组，可为空。 */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbStringTypeHandler.class)
    private String keyPointsJson;

    /** 出题时保存的单题评分标准。 */
    private String scoringRubric;

    /** 专项主问题的候选追问快照，内容为 JSON 数组，可为空。 */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbStringTypeHandler.class)
    private String followUpCandidatesJson;

    /** 用户正式提交的答案，未作答时为空。 */
    private String userAnswer;

    /** 该题目加入本场面试的时间。 */
    private OffsetDateTime createdAt;

    /** 用户正式提交答案的时间，未作答时为空。 */
    private OffsetDateTime answeredAt;
}
