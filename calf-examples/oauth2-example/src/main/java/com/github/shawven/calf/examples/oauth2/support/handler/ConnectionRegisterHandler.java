package com.github.shawven.calf.examples.oauth2.support.handler;

import com.github.shawven.calf.examples.oauth2.domain.User;
import com.github.shawven.calf.examples.oauth2.domain.UserRegisterRequest;
import com.github.shawven.calf.examples.oauth2.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.stereotype.Component;

/**
 * 第三方登录自动注册
 *
 * @author Shoven
 * @date 2019-11-16
 */
@Component
public class ConnectionRegisterHandler implements ConnectionSignUp {

    @Autowired
    private UserService userService;

    @Override
    public String execute(Connection<?> connection) {
        UserRegisterRequest request = new UserRegisterRequest();
        // 设置随机用户名
        request.setUsername(getUsername(connection.getKey()));
        request.setNickname(connection.getDisplayName());
        request.setAvatar(connection.getImageUrl());
        // 设置随机密码
        request.setPassword(RandomStringUtils.randomAlphanumeric(32));
        User user = userService.register(request);
        return user.getId().toString();
    }

    private String getUsername(ConnectionKey key) {
        return " wq_" + key.getProviderId() + "_" + key.getProviderUserId();
    }
}
