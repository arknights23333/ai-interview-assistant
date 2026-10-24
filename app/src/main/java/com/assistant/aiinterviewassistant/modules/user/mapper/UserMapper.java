package com.assistant.aiinterviewassistant.modules.user.mapper;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserPo>{
}
