package com.test.security.browser.property;

/**
 * @author Shoven
 * @date 2019-08-20
 */
public interface BrowserConstants {
    /**
     * 默认请求登陆跳转的url
     */
    String DEFAULT_SIGN_IN_URL = "/login";

    /**
     * 默认的用户名密码登录请求处理url
     */
    String DEFAULT_SIGN_IN_PROCESSING_URL_FORM = "/login/form";

    /**
     * 默认退出登录的url
     */
    String DEFAULT_SIGN_OUT_URL = "/logout";

    /**
     * session失效默认的跳转地址
     */
    String DEFAULT_SESSION_INVALID_URL = "/session-invalid.html";
}
