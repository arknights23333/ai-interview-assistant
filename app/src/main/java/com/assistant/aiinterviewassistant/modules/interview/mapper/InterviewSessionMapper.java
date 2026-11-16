package com.assistant.aiinterviewassistant.modules.interview.mapper;

import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewSessionMapper extends BaseMapper<InterviewSessionPo> {
}
