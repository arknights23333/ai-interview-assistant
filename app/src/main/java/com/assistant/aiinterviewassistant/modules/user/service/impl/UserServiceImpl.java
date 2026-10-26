package com.assistant.aiinterviewassistant.modules.user.service.impl;

import com.assistant.aiinterviewassistant.modules.user.constant.UserStatusConstant;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.mapper.UserMapper;
import com.assistant.aiinterviewassistant.modules.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户业务服务实现。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPo> implements UserService {



    public UserPo getUserByUsername(String username) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<UserPo>()
                .eq(UserPo::getUsername, username)
                .eq(UserPo::getStatus, UserStatusConstant.ACTIVE)
        );
    }
}
