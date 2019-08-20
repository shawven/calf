
package com.test.security.browser.config;

import com.test.security.browser.property.BrowserProperties;
import com.test.security.base.authentication.configurer.AuthorizationConfigurerManager;
import com.test.security.oauth2.config.SmsCodeAuthenticationSecurityConfig;
import com.test.security.validate.config.ValidationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.social.security.SpringSocialConfigurer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 浏览器环境下安全配置主类
 *
 * @author Shoven
 * @since 2019-05-08 21:53
 */
@Component
public class BrowserSecurityConfiguration {

    @Autowired
    private BrowserProperties browserProperties;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;

    @Autowired
    private ValidationSecurityConfig validationSecurityConfig;

	@Autowired
	private SpringSocialConfigurer springSocialConfigurer;

	@Autowired
	private SessionInformationExpiredStrategy sessionInformationExpiredStrategy;

	@Autowired
	private InvalidSessionStrategy invalidSessionStrategy;

	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;

	@Autowired
	private AuthorizationConfigurerManager authorizationConfigurerManager;

	@Autowired
	private LoginSecurityConfig loginSecurityConfig;

    @Autowired
    private AccessDeniedHandler browserAccessDeniedHandler;

    @Autowired
    private AuthenticationEntryPoint browserAuthenticationExceptionEntryPoint;

    public void configureWeb(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/**/favicon.ico",
                "/**/*.js",
                "/**/*.css",
                "/**/*.jpg",
                "/**/*.png",
                "/**/*.gif"
        );
    }

	public void configureHttp(HttpSecurity http) throws Exception {

		loginSecurityConfig.configure(http);

		http
            .exceptionHandling()
                .authenticationEntryPoint(browserAuthenticationExceptionEntryPoint)
                .accessDeniedHandler(browserAccessDeniedHandler)
                .and()
            .apply(validationSecurityConfig)
				.and()
			.apply(smsCodeAuthenticationSecurityConfig)
				.and()
			.apply(springSocialConfigurer)
				.and()
			//记住我配置，如果想在'记住我'登录时记录日志，可以注册一个InteractiveAuthenticationSuccessEvent事件的监听器
//			.rememberMe()
//				.tokenRepository(persistentTokenRepository())
//				.tokenValiditySeconds(securityProperties.getBrowser().getRememberMeSeconds())
//				.userDetailsService(userDetailsService)
//				.and()
			.sessionManagement()
//				.invalidSessionStrategy(invalidSessionStrategy)
				.maximumSessions(browserProperties.getSession().getMaximumSessions())
				.maxSessionsPreventsLogin(browserProperties.getSession().isMaxSessionsPreventsLogin())
				.expiredSessionStrategy(sessionInformationExpiredStrategy)
				.and()
				.and()
			.logout()
				.logoutUrl(browserProperties.getSignOutUrl())
				.logoutSuccessHandler(logoutSuccessHandler)
				.deleteCookies("JSESSIONID")
				.and()
			.csrf().disable();

        authorizationConfigurerManager.config(http.authorizeRequests());
	}
//
//	/**
//	 * 记住我功能的token存取器配置
//	 * @return
//	 */
//	@Bean
//	public PersistentTokenRepository persistentTokenRepository() {
//		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
//		tokenRepository.setDataSource(dataSource);
////		tokenRepository.setCreateTableOnStartup(true);
//		return tokenRepository;
//	}

}
