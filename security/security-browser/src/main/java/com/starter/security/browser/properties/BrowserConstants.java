package com.starter.security.browser.properties;

/**
 * @author Shoven
 * @date 2019-08-20
 */
public interface BrowserConstants {
    /**
     * 默认的登陆url
     */
    String DEFAULT_SIGN_IN_URL = "/signIn.html";

    /**
     * 默认的登录请求处理url
     */
    String DEFAULT_SIGN_IN_PROCESSING_URL_FORM = "/login";

    /**
     * 默认的登录成功后跳转的url
     */
    String DEFAULT_SIGN_IN_SUCCESS_URL = "index.html";

    /**
     * 默认退出登录的url
     */
    String DEFAULT_SIGN_OUT_PROCESSING_URL = "/logout";

    /**
     * 默认退出登录成功的url
     */
    String DEFAULT_SIGN_OUT_SUCCESS_URL = "/signOut.html";

    /**
     * 默认注册url
     */
    String DEFAULT_SIGN_UP_URL = "/signUp.html";

    /**
     * session失效默认的跳转url
     */
    String DEFAULT_SESSION_INVALID_URL = "/session-invalid.html";
}
