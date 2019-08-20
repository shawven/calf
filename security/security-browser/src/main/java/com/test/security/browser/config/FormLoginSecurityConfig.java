
package com.test.security.browser.config;

import com.test.security.browser.property.BrowserProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 表单登录配置
 *
 */
public class FormLoginSecurityConfig {

    private BrowserProperties browserProperties;

    protected AuthenticationSuccessHandler browserAuthenticationSuccessHandler;

    protected AuthenticationFailureHandler browserAuthenticationFailureHandler;

    public FormLoginSecurityConfig(BrowserProperties browserProperties,
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
