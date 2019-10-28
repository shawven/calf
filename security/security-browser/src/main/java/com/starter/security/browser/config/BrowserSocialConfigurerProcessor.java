package com.starter.security.browser.config;

import com.starter.security.social.properties.SocialConstants;
import com.starter.security.social.support.SocialConfigurer;
import com.starter.security.social.support.SocialConfigurerProcessor;

/**
 * 浏览器的社交配置器的处理器
 *
 * @author Shoven
 * @since 2019-04-20 15:37
 */
public class BrowserSocialConfigurerProcessor implements SocialConfigurerProcessor {


    /**
     *  假如没有配置无感知处理程序connectionSignUp，需要引导用户进行注册或绑定
     *  在浏览器环境下时第一次社交登录时会跳转配置的注册页面
     *  app环境下跳到该接口存储用户信息备用，让app去跳转到注册页再来拿这个信息
     *
     * @param configurer
     */
    @Override
    public void proceed(SocialConfigurer configurer) {

        configurer.signupUrl(SocialConstants.DEFAULT_CURRENT_SOCIAL_USER_INFO_URL);
    }
}
