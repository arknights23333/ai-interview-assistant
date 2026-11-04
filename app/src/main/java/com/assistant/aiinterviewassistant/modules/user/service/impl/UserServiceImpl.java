package com.assistant.aiinterviewassistant.modules.user.service.impl;

import com.assistant.aiinterviewassistant.common.exception.enums.UserErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.user.constant.UserStatusConstant;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.OffsetDateTime;
import org.springframework.beans.BeanUtils;
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



    @Override
    public UserVo getCurrentUser(Long userId) {
        UserPo user = getById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        UserVo result = new UserVo();
        BeanUtils.copyProperties(user, result);
        return result;
    }

    @Override
    public void updateNickname(Long userId, String nickname) {
        boolean updated = update(new LambdaUpdateWrapper<UserPo>()
                .eq(UserPo::getId, userId)
                .set(UserPo::getNickname, nickname)
                .set(UserPo::getUpdatedAt, OffsetDateTime.now()));
        if (!updated) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    @Override
    public UserPo getUserByIdOrThrow(Long userId) {
        UserPo user = getById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public UserPo getActiveUserByIdOrThrow(Long userId) {
        UserPo user = getUserByIdOrThrow(userId);
        if (!UserStatusConstant.ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(UserErrorCode.USER_DISABLED);
        }
        return user;
    }
    public UserPo getUserByUsername(String username) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<UserPo>()
                .eq(UserPo::getUsername, username)
        );
    }
}
