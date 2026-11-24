package com.assistant.aiinterviewassistant.modules.questionbank.mapper;

import com.assistant.aiinterviewassistant.modules.questionbank.domain.po.QuestionBankQuestionPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 题库题目数据访问接口。 */
public interface QuestionBankQuestionMapper extends BaseMapper<QuestionBankQuestionPo> {

    /** 删除本人指定知识库下选中的题目，并返回实际删除的目标 ID。 */
    @Select(value = """
            <script>
            DELETE FROM question_bank_question AS q
            USING knowledge_base AS kb
            WHERE q.knowledge_base_id = kb.id
              AND kb.user_id = #{userId}
              AND q.knowledge_base_id = #{knowledgeBaseId}
              AND q.id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">
                  #{id}
              </foreach>
            RETURNING q.id
            </script>
            """, affectData = true)
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Long> deleteBatchAndReturnIds(
            @Param("userId") Long userId,
            @Param("knowledgeBaseId") Long knowledgeBaseId,
            @Param("ids") List<Long> ids);
}
