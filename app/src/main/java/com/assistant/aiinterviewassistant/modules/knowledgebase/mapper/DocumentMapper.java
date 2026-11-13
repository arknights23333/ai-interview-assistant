package com.assistant.aiinterviewassistant.modules.knowledgebase.mapper;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentMapper  extends BaseMapper<DocumentPo> {

    @Select(value = """
            <script>
            DELETE FROM document AS d
            USING knowledge_base AS kb
            WHERE d.knowledge_base_id = kb.id
              AND kb.user_id = #{userId}
              AND d.knowledge_base_id = #{knowledgeBaseId}
              AND d.id IN
              <foreach collection="target" item="id" open="(" separator="," close=")">
                  #{id}
              </foreach>
            RETURNING d.id, d.storage_key
            </script>
            """, affectData=true)
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<DocumentPo> deleteBatchAndReturn(
            @Param("target") List<Long> target,
            @Param("userId") Long userId,
            @Param("knowledgeBaseId") Long knowledgeBaseId
    );
    @Select(value = """
       <script>
       DELETE FROM document AS d
            USING knowledge_base AS kb
            WHERE d.knowledge_base_id = kb.id
              AND kb.user_id = #{userId}
              AND d.knowledge_base_id = #{knowledgeBaseId}
       RETURNING d.id, d.storage_key
       </script>
        """,affectData = true)
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<DocumentPo> deleteDocumentByKnowledgeBaseAndReturn(
            @Param("userId") Long userId,
            @Param("knowledgeBaseId") Long knowledgeBaseId
    );
}
