package com.starter.security.browser.config;

import com.starter.security.social.support.SocialAuthenticationFilterPostProcessor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.social.security.SocialAuthenticationFilter;

/**
 * 浏览器的社交配置器的处理器
 *
 * @author Shoven
 * @since 2019-04-20 15:37
 */
public class BrowserSocialAuthenticationFilterPostProcessor implements SocialAuthenticationFilterPostProcessor {


    private AuthenticationSuccessHandler browserAuthenticationSuccessHandler;
    private AuthenticationFailureHandler browserAuthenticationFailureHandler;

    public BrowserSocialAuthenticationFilterPostProcessor(AuthenticationSuccessHandler browserAuthenticationSuccessHandler,
                                                          AuthenticationFailureHandler browserAuthenticationFailureHandler) {
        this.browserAuthenticationSuccessHandler = browserAuthenticationSuccessHandler;
        this.browserAuthenticationFailureHandler = browserAuthenticationFailureHandler;
    }

    /**
     * 修改成功获取openid后的成功处理器，浏览器会发起页面跳转到
     * 这里是app环境，要直接返回token，避免接口请求返回304
     *
     * @param socialAuthenticationFilter
     */
    @Override
    public void proceed(SocialAuthenticationFilter socialAuthenticationFilter) {
        socialAuthenticationFilter.setAuthenticationSuccessHandler(browserAuthenticationSuccessHandler);
        socialAuthenticationFilter.setAuthenticationFailureHandler(browserAuthenticationFailureHandler);
    }
}
