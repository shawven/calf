package com.github.shawven.calf.examples.oauth2.support;

/**
 * @author Shoven
 * @date 2020-03-04
 */
public interface AuthenticationContext {

    /**
     * 获取用户ID
     *
     * @return
     */
    Long getUserId();

    /**
     * 获取客户端ID
     *
     * @return
     */
    String getClientId();

    /**
     * 获取sessionId
     *
     * @return
     */
    String getSessionId();
}
