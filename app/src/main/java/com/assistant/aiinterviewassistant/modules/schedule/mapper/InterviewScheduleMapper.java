package com.assistant.aiinterviewassistant.modules.schedule.mapper;
import com.assistant.aiinterviewassistant.modules.schedule.domain.po.InterviewSchedulePo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InterviewScheduleMapper extends BaseMapper<InterviewSchedulePo> {
    @Select(value = """
            <script>
            DELETE FROM interview_schedule
            WHERE user_id = #{userId}
              AND id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
              </foreach>
            RETURNING id
            </script>
            """, affectData = true)
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Long> deleteSchedule(@Param("userId")Long userId, @Param("ids")List<Long> ids);
}
