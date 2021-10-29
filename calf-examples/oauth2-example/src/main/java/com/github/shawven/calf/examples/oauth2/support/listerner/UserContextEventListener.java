package com.github.shawven.calf.examples.oauth2.support.listerner;


import com.github.shawven.calf.examples.oauth2.domain.User;
import com.github.shawven.calf.examples.oauth2.service.RbacService;
import com.github.shawven.calf.examples.oauth2.support.AuthenticationContext;
import com.github.shawven.calf.examples.oauth2.support.CtxData;
import com.github.shawven.calf.examples.oauth2.support.CtxDataAccessor;
import com.github.shawven.calf.examples.oauth2.support.event.RefreshContextPermissionEvent;
import com.github.shawven.calf.examples.oauth2.support.event.RefreshContextUserEvent;
import com.github.shawven.calf.examples.oauth2.support.event.LoginSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户上下文事件监听器
 *
 * @author Shoven
 * @date 2019-11-24
 */
@Slf4j
@Component
public class UserContextEventListener {

    @Autowired
    private CtxDataAccessor ctxDataAccessor;

    @Autowired
    private AuthenticationContext authenticationContext;

    @Autowired
    private RbacService rbacService;

    @EventListener
    public void onLoginSuccessEvent(LoginSuccessEvent event) {
        User user = event.getUser();
        Long userId = user.getId();
        log.info("用户[{}]登录成功事件监听：{}", userId, event);

        // 刷新上下文用户
        refreshContextUser(user);
        // 设置客户端会话
        setClientSession(userId);
    }

    /**
     * 设置客户端会话
     *  @param userId
     */
    private void setClientSession(Long userId) {
        CtxData ctxData = ctxDataAccessor.getContextData(userId, false);
        Map<String, String> session = ctxData.getClientSession();
        // 不存在则添加
        if (session == null) {
            session = new HashMap<>(1);
        }
        String clientId = authenticationContext.getClientId();
        String sessionId = authenticationContext.getSessionId();
        session.put(clientId, sessionId);
        ctxData.setClientSession(session).save();
    }


    @EventListener
    public void onRefreshContextUser(RefreshContextUserEvent event) {
        Long userId = event.getUser().getId();
        log.debug("刷新用户[{}]上下文用户事件监听：{}", userId, event);
        refreshContextUser(event.getUser());
    }

    private void refreshContextUser(User user) {
        CtxData ctxData;
        if (ctxDataAccessor.exist(user.getId())) {
            ctxData = ctxDataAccessor.getContextData(user.getId(), false);
        } else {
            ctxData = CtxData.anonymous().setCtxDataAccessor(ctxDataAccessor);
        }
        ctxData.setUser(user);
        ctxData.save();
        log.info("加载用户[{}]上下文基本信息完成", user.getId());
    }

    @EventListener
    public void onRefreshRefreshContextPermissions(RefreshContextPermissionEvent event) {
        Long userId = event.getUserId();
        List<Integer> permissionIds = event.getPermissionIds();
        log.debug("刷新用户[{}]上下文权限事件监听", userId);
        refreshPermissionsToContext(userId, permissionIds);
    }

    private void refreshPermissionsToContext(Long userId, List<Integer> permissionIds) {
        // 持久化
        ctxDataAccessor.getContextData(userId).setPermissions(permissionIds).save();
        log.info("加载用户[{}]上下文权限信息完成", userId);
    }
}
