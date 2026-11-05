package com.assistant.aiinterviewassistant.modules.resume.mapper;

import com.assistant.aiinterviewassistant.modules.resume.domain.po.ResumePo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ResumeMapper extends BaseMapper<ResumePo> {

    /** 彻底删除本次选中的回收站记录，返回实际删除的 ID 和对象键。 */
    @Select(value = """
            <script>
            DELETE FROM resume
            WHERE user_id = #{userId}
              AND delete_time IS NOT NULL
              AND id IN
              <foreach collection="resumeIds" item="resumeId" open="(" separator="," close=")">
                #{resumeId}
              </foreach>
            RETURNING id, storage_key AS "storageKey"
            </script>
            """, affectData = true)
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<ResumePo> deleteRecycledReturning(
            @Param("userId") Long userId, @Param("resumeIds") List<Long> resumeIds);
}
