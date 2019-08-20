package com.test.security.app.config.social;

import com.test.security.app.authentication.AppSocailAuthenticationFailureHandler;
import com.test.security.social.support.SocialAuthenticationFilterPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.social.security.SocialAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 * 社交过滤器的后处理器，用于在不同环境下个性化社交登录的配置
 *
 * @author Shoven
 * @since 2019-04-19 10:15
 */
@Component
public class AppSocialAuthenticationFilterPostProcessImpl implements SocialAuthenticationFilterPostProcessor {

    @Autowired
    private AuthenticationSuccessHandler appAuthenticationSuccessHandler;

    /**
     * 修改成功获取openid后的成功处理器，浏览器会发起页面跳转到
     * 这里是app环境，要直接返回token，避免接口请求返回304
     *
     * @param socialAuthenticationFilter
     */
    @Override
    public void process(SocialAuthenticationFilter socialAuthenticationFilter) {
        socialAuthenticationFilter.setAuthenticationSuccessHandler(appAuthenticationSuccessHandler);
        socialAuthenticationFilter.setAuthenticationFailureHandler(new AppSocailAuthenticationFailureHandler());
    }
}
