
package com.test.security.browser.config;

import com.test.security.base.authentication.configurer.AuthorizationConfigurerProvider;
import com.test.security.browser.authentication.*;
import com.test.security.browser.properties.BrowserProperties;
import com.test.security.browser.session.BrowserExpiredSessionStrategy;
import com.test.security.browser.session.BrowserInvalidSessionStrategy;
import com.test.security.browser.session.SessionVerificationRepository;
import com.test.security.social.annotaion.EnableSocialSupport;
import com.test.security.social.properties.SocialProperties;
import com.test.security.verification.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * 浏览器环境下扩展点配置，配置在这里的bean，业务系统都可以通过声明同类型或同名的bean来覆盖安全
 * 模块默认的配置。
 *
 * @author Shoven
 * @since 2019-05-08 21:53
 */
@Configuration
@EnableSocialSupport
public class BrowserConfiguration {

	@Autowired
	private BrowserProperties browserProperties;

	@Autowired
    private SocialProperties socialProperties;

    /**
     * 基于session的验证码存取器
     *
     * @return
     */
	@Bean
    @ConditionalOnMissingBean
    public VerificationRepository sessionVerificationRepository() {
	    return new SessionVerificationRepository();
    }

	/**
	 * session失效时的处理策略配置
     *
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public InvalidSessionStrategy invalidSessionStrategy(){
		return new BrowserInvalidSessionStrategy(browserProperties);
	}

	/**
	 * 并发登录导致前一个session失效时的处理策略配置
     *
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public SessionInformationExpiredStrategy sessionInformationExpiredStrategy(){
		return new BrowserExpiredSessionStrategy(browserProperties);
	}

	/**
	 * 退出时的处理策略配置
	 *
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public LogoutSuccessHandler logoutSuccessHandler(){
		return new BrowserLogoutSuccessHandler(browserProperties.getSignOutSuccessUrl());
	}

    /**
     * 验证成功处理器
     *
     * @param loginSuccessHandler
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationSuccessHandler browserAuthenticationSuccessHandler(@Autowired(required = false)
                                                                                    LoginSuccessHandler loginSuccessHandler) {
        return new BrowserAuthenticationSuccessHandler(browserProperties, loginSuccessHandler);
    }

    /**
     * 验证失败处理器
     *
     * @return
     */
	@Bean
    @ConditionalOnMissingBean
    public AuthenticationFailureHandler browserAuthenticationFailureHandler() {
	    return new BrowserAuthenticationFailureHandler(browserProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AccessDeniedHandler browserAccessDeniedHandler() {
	    return new BrowserAccessDeniedHandler(browserProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationEntryPoint browserAuthenticationExceptionEntryPoint() {
        return new BrowserAuthenticationExceptionEntryPoint(browserProperties);
    }

    /**
     * 表单登陆安全配置
     *
     * @param authenticationSuccessHandler
     * @param authenticationFailureHandler
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public LoginSecurityConfig FormLoginSecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler,
                                                       AuthenticationFailureHandler authenticationFailureHandler) {
	    return new LoginSecurityConfig(browserProperties, authenticationSuccessHandler, authenticationFailureHandler);
    }

    /**
     * 授权配置提供器
     *
     * @return
     */
    @Bean
    @Order(Integer.MIN_VALUE)
    @ConditionalOnMissingBean
    public AuthorizationConfigurerProvider authorizationConfigurerProvider() {
	    return new BrowserAuthorizationConfigurerProvider(browserProperties, socialProperties);
    }

}
