package com.github.shawven.calf.examples.oauth2.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Shoven
 * @date 2020-03-17
 */
@Component
public class ContextImpl implements Context {

    @Autowired
    private AuthenticationContext authenticationContext;

    @Autowired
    private CtxDataAccessor ctxDataAccessor;

    /**
     * 获取上下文数据, 如果只获取用户ID，建议用 UserContext.getUserId()
     *
     * @return ContextData
     */
    @Override
    public CtxData get() {
        Long userId = getUserId();
        if (Objects.equals(userId, 0L)) {
            return CtxData.anonymous().setCtxDataAccessor(ctxDataAccessor);
        }
        return ctxDataAccessor.getContextData(userId);
    }


    /**
     * 获取用户ID，只获取用户ID无需操作Redis
     *
     * @return 用户ID 返回0说明用户未登录是匿名用户
     */
    @Override
    public Long getUserId() {
        return authenticationContext.getUserId();
    }
}
