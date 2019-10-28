
package com.starter.security.browser.config;

import com.starter.security.base.authentication.configurer.AuthorizationConfigurerManager;
import com.starter.security.browser.properties.BrowserProperties;
import com.starter.security.social.config.SmsAuthenticationSecurityConfigurer;
import com.starter.security.verification.config.VerificationSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.social.security.SpringSocialConfigurer;

import javax.sql.DataSource;

/**
 * 浏览器环境下安全配置主类
 *
 * @author Shoven
 * @since 2019-05-08 21:53
 */
public class BrowserSecurityConfigurer {

    private BrowserProperties browserProperties;

	private DataSource dataSource;

	private UserDetailsService userDetailsService;

	private SmsAuthenticationSecurityConfigurer smsAuthenticationSecurityConfigurer;

    private VerificationSecurityConfigurer verificationSecurityConfigurer;

	private SpringSocialConfigurer springSocialConfigurer;

	private SessionInformationExpiredStrategy sessionInformationExpiredStrategy;

	private InvalidSessionStrategy invalidSessionStrategy;

	private LogoutSuccessHandler logoutSuccessHandler;

	private AuthorizationConfigurerManager authorizationConfigurerManager;

	private FormLoginSecurityConfigurer formLoginSecurityConfigurer;

    private AccessDeniedHandler browserAccessDeniedHandler;

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
		http
            .exceptionHandling()
                .authenticationEntryPoint(browserAuthenticationExceptionEntryPoint)
                .accessDeniedHandler(browserAccessDeniedHandler)
                .and()
            .apply(verificationSecurityConfigurer)
				.and()
			.apply(smsAuthenticationSecurityConfigurer)
				.and()
			.apply(springSocialConfigurer)
		        .and()
			.sessionManagement()
//				.invalidSessionStrategy(invalidSessionStrategy)
				.maximumSessions(browserProperties.getSession().getMaximumSessions())
				.maxSessionsPreventsLogin(browserProperties.getSession().isMaxSessionsPreventsLogin())
				.expiredSessionStrategy(sessionInformationExpiredStrategy)
				.and()
				.and()
			.logout()
				.logoutUrl(browserProperties.getSignOutProcessingUrl())
				.logoutSuccessHandler(logoutSuccessHandler)
				.deleteCookies("JSESSIONID")
				.and()
			.csrf().disable();

        //记住我配置，如果想在'记住我'登录时记录日志，可以注册一个InteractiveAuthenticationSuccessEvent事件的监听器
        int rememberMeSeconds = browserProperties.getRememberMeSeconds();
        if (rememberMeSeconds > 0) {
            http
                .rememberMe()
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(browserProperties.getRememberMeSeconds())
                .userDetailsService(userDetailsService);
        }

        formLoginSecurityConfigurer.configure(http);
        authorizationConfigurerManager.config(http.authorizeRequests());
	}

	/**
	 * 记住我功能的token存取器配置
	 * @return
	 */
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
//		tokenRepository.setCreateTableOnStartup(true);
		return tokenRepository;
	}

    public BrowserProperties getBrowserProperties() {
        return browserProperties;
    }

    public void setBrowserProperties(BrowserProperties browserProperties) {
        this.browserProperties = browserProperties;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public SmsAuthenticationSecurityConfigurer getSmsAuthenticationSecurityConfigurer() {
        return smsAuthenticationSecurityConfigurer;
    }

    public void setSmsAuthenticationSecurityConfigurer(SmsAuthenticationSecurityConfigurer smsAuthenticationSecurityConfigurer) {
        this.smsAuthenticationSecurityConfigurer = smsAuthenticationSecurityConfigurer;
    }

    public VerificationSecurityConfigurer getVerificationSecurityConfigurer() {
        return verificationSecurityConfigurer;
    }

    public void setVerificationSecurityConfigurer(VerificationSecurityConfigurer verificationSecurityConfigurer) {
        this.verificationSecurityConfigurer = verificationSecurityConfigurer;
    }

    public SpringSocialConfigurer getSpringSocialConfigurer() {
        return springSocialConfigurer;
    }

    public void setSpringSocialConfigurer(SpringSocialConfigurer springSocialConfigurer) {
        this.springSocialConfigurer = springSocialConfigurer;
    }

    public SessionInformationExpiredStrategy getSessionInformationExpiredStrategy() {
        return sessionInformationExpiredStrategy;
    }

    public void setSessionInformationExpiredStrategy(SessionInformationExpiredStrategy sessionInformationExpiredStrategy) {
        this.sessionInformationExpiredStrategy = sessionInformationExpiredStrategy;
    }

    public InvalidSessionStrategy getInvalidSessionStrategy() {
        return invalidSessionStrategy;
    }

    public void setInvalidSessionStrategy(InvalidSessionStrategy invalidSessionStrategy) {
        this.invalidSessionStrategy = invalidSessionStrategy;
    }

    public LogoutSuccessHandler getLogoutSuccessHandler() {
        return logoutSuccessHandler;
    }

    public void setLogoutSuccessHandler(LogoutSuccessHandler logoutSuccessHandler) {
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    public AuthorizationConfigurerManager getAuthorizationConfigurerManager() {
        return authorizationConfigurerManager;
    }

    public void setAuthorizationConfigurerManager(AuthorizationConfigurerManager authorizationConfigurerManager) {
        this.authorizationConfigurerManager = authorizationConfigurerManager;
    }

    public FormLoginSecurityConfigurer getFormLoginSecurityConfigurer() {
        return formLoginSecurityConfigurer;
    }

    public void setFormLoginSecurityConfigurer(FormLoginSecurityConfigurer formLoginSecurityConfigurer) {
        this.formLoginSecurityConfigurer = formLoginSecurityConfigurer;
    }

    public AccessDeniedHandler getBrowserAccessDeniedHandler() {
        return browserAccessDeniedHandler;
    }

    public void setBrowserAccessDeniedHandler(AccessDeniedHandler browserAccessDeniedHandler) {
        this.browserAccessDeniedHandler = browserAccessDeniedHandler;
    }

    public AuthenticationEntryPoint getBrowserAuthenticationExceptionEntryPoint() {
        return browserAuthenticationExceptionEntryPoint;
    }

    public void setBrowserAuthenticationExceptionEntryPoint(AuthenticationEntryPoint browserAuthenticationExceptionEntryPoint) {
        this.browserAuthenticationExceptionEntryPoint = browserAuthenticationExceptionEntryPoint;
    }
}
