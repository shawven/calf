package com.github.shawven.calf.examples.oauth2.support;

/**
 * 用户上下文
 *
 * @author Shoven
 * @date 2019-07-05 14:22
 */
public interface Context {

    /**
     * 获取上下文数据, 如果只获取用户ID，建议用 UserContext.getUserId()
     *
     * @return ContextData
     */
    CtxData get();


    /**
     * 获取用户ID，只获取用户ID无需操作Redis
     *
     * @return 用户ID 返回0说明用户未登录是匿名用户
     */
    Long getUserId();
}
