
package com.starter.security.browser.config;

import com.starter.security.browser.properties.BrowserProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 表单登录配置
 *
 */
public class LoginSecurityConfig {

    private BrowserProperties browserProperties;

    protected AuthenticationSuccessHandler browserAuthenticationSuccessHandler;

    protected AuthenticationFailureHandler browserAuthenticationFailureHandler;

    public LoginSecurityConfig(BrowserProperties browserProperties,
                               AuthenticationSuccessHandler browserAuthenticationSuccessHandler,
                               AuthenticationFailureHandler browserAuthenticationFailureHandler) {
        this.browserProperties = browserProperties;
        this.browserAuthenticationSuccessHandler = browserAuthenticationSuccessHandler;
        this.browserAuthenticationFailureHandler = browserAuthenticationFailureHandler;
    }

    public void configure(HttpSecurity http) throws Exception {
		http.formLogin()
                .loginPage(browserProperties.getSignInUrl())
                .loginProcessingUrl(browserProperties.getSignInProcessUrl())
                .successHandler(browserAuthenticationSuccessHandler)
                .failureHandler(browserAuthenticationFailureHandler);
	}
}
