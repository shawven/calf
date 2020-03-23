package com.starter.demo.support.listerner;


import com.starter.demo.domain.User;
import com.starter.demo.support.event.LoginFailureEvent;
import com.starter.demo.support.event.LoginSuccessEvent;
import com.starter.demo.support.event.LogoutEvent;
import com.starter.demo.support.event.RegisterEvent;
import com.starter.demo.service.UserService;
import com.starter.demo.support.CtxDataAccessor;
import com.starter.demo.support.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 用户事件监听器
 *
 * @author Shoven
 * @date 2019-11-24
 */
@Slf4j
@Component
public class UserEventListener {

    @Autowired
    private UserService userService;

    @Autowired
    private CtxDataAccessor ctxDataAccessor;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @EventListener
    public void onLoginSuccessEvent(LoginSuccessEvent event) {
        taskExecutor.execute(() -> {
            log.info("用户登录成功事件监听：{}", event);
            User user = event.getUser();

            HttpServletRequest request = event.getRequest();

            User update = new User();
            update.setId(user.getId());
            update.setLastLoginIp(user.getLoginIp());
            update.setLoginIp(IpUtils.getIp(request));

            update.setLastLoginTime(user.getLoginTime());
            update.setLoginTime(new Date());
            update.setErrorCount(0);

            userService.updateLoginRecord(update);
            log.info("已记录用户[{}]登录信息", user.getId());
        });
    }

    @EventListener
    public void onLoginFailureEvent(LoginFailureEvent event) {
        log.info("用户登录失败事件监听：{}", event);
        String principal = event.getPrincipal();
        User user = userService.getByUsername(principal);
        userService.disabledIfReachTheMaxAttempts(user);
    }

    @EventListener
    public void onRegisterEvent(RegisterEvent event) {
        log.info("用户注册事件监听：{}", event);
        User user = event.getUser();


        // send notification to user
    }

    @EventListener
    public void onLogoutEvent(LogoutEvent event) {
        log.info("用户注销事件监听：{}", event);
        ctxDataAccessor.logout(event.getUserId());
    }
}
