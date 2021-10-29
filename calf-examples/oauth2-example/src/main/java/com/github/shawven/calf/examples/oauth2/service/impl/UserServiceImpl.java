package com.github.shawven.calf.examples.oauth2.service.impl;


import com.github.shawven.calf.examples.oauth2.domain.User;
import com.github.shawven.calf.examples.oauth2.domain.UserProfileDTO;
import com.github.shawven.calf.examples.oauth2.domain.UserRegisterRequest;
import com.github.shawven.calf.examples.oauth2.domain.UserUpdateRequest;
import com.github.shawven.calf.examples.oauth2.mapper.UserMapper;
import com.github.shawven.calf.examples.oauth2.service.UserService;
import com.github.shawven.calf.examples.oauth2.support.event.RefreshContextUserEvent;
import com.github.shawven.calf.examples.oauth2.support.event.RegisterEvent;
import com.github.shawven.calf.examples.oauth2.support.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 操作员表 服务实现类
 * </p>
 *
 * @author Shoven
 * @date 2019-10-26
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        return new UserProfileDTO(user, true);
    }

    @Override
    public User register(UserRegisterRequest request) {
        // 校验
        request.validate();

        User newUser = request.newUser();
        String phone = newUser.getPhone();
        if (StringUtils.isNotBlank(phone) && existByPhone(phone)) {
            throw new BizException("手机号已存在");
        }
        String username = newUser.getUsername();
        if (StringUtils.isNotBlank(username)) {
            if (existByUsername(username)) {
                throw new BizException("用户名已存在");
            }
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        userMapper.insert(newUser);

        // 注册事件
        eventPublisher.publishEvent(new RegisterEvent(newUser));
        return newUser;
    }

    @Override
    public boolean updateUser(UserUpdateRequest request) {
        User newUser = request.newUser();
        String username = newUser.getUsername();

        if (StringUtils.isNotBlank(username)) {
            User existUser = getByUsername(username);
            if (existUser != null && !existUser.getId().equals(newUser.getId())
                    &&existUser.getUsername().equals(username)) {
                throw new BizException("用户名已存在");
            }
        }
        return userMapper.updateById(newUser) > 0;
    }

    @Override
    public boolean updatePhone(Long userId, String phone) {
        User user = userMapper.selectById(userId);
        if (existByPhone(phone)) {
            throw new BizException("手机号已存在");
        }
        // 刷新用户上下文
        eventPublisher.publishEvent(new RefreshContextUserEvent(user));

        User update = new User().setId(userId).setPhone(phone);
        return userMapper.updateById(update) > 0;
    }

    @Override
    public boolean resetPassword(String phone, String password) {
        User user = getByPhone(phone);
        if (user == null) {
            throw new BizException("该手机号未注册账号");
        }
        User update = new User()
                .setId(user.getId())
                .setPassword(passwordEncoder.encode(password));
        return userMapper.updateById(update) > 0;
    }

    @Override
    public void disabledIfReachTheMaxAttempts(User user) {
        if (user == null) {
            return;
        }
        // 设置错误的密码次数
        user.setErrorCount(user.getErrorCount() + 1);
        // 禁止登录
        if (user.getErrorCount() == 5) {
            user.setIsDisabled(true);
            userMapper.updateErrorCountAndDisabled(user);
        }
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByPhone(String phone) {
        return userMapper.selectOne(new User().setPhone(phone));
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(new User().setUsername(username));
    }

    @Override
    public List<User> listUsersByIds(List<Long> ids) {
        return userMapper.selectBatchIds(ids);
    }

    @Override
    public boolean existByPhone(String phone) {
        return userMapper.selectCount(new User().setPhone(phone)) >= 1;
    }

    @Override
    public boolean existByUsername(String username) {
        return userMapper.selectCount(new User().setUsername(username)) >= 1;
    }

    @Override
    public boolean existByEmail(String email) {
        return userMapper.selectCount(new User().setEmail(email)) >= 1;
    }

    @Override
    public boolean updateLoginRecord(User user) {
        return userMapper.updateLoginRecord(user);
    }
}
