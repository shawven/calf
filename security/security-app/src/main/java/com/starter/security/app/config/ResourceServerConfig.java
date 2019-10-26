
package com.starter.security.app.config;

import com.starter.security.base.authentication.configurer.AuthorizationConfigurerManager;
import com.starter.security.oauth2.config.SmsAuthenticationSecurityConfig;
import com.starter.security.verification.config.VerificationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.social.security.SpringSocialConfigurer;

/**
 * 资源服务器配置
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private SmsAuthenticationSecurityConfig smsAuthenticationSecurityConfig;

    @Autowired
    private OpenIdAuthenticationSecurityConfig openIdAuthenticationSecurityConfig;

    @Autowired
    private VerificationSecurityConfig verificationSecurityConfig;

    @Autowired
    private SpringSocialConfigurer socialSecurityConfig;

    @Autowired
    private WxMiniAuthenticationSecurityConfig wxMiniAuthenticationSecurityConfig;

    @Autowired
    private AuthorizationConfigurerManager authorizationConfigurerManager;

    @Autowired
    private AccessDeniedHandler appAccessDeniedHandler;

    @Autowired
    private AuthenticationEntryPoint appOAuth2AuthenticationExceptionEntryPoint;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources
                .authenticationEntryPoint(appOAuth2AuthenticationExceptionEntryPoint)
                .accessDeniedHandler(appAccessDeniedHandler);
        super.configure(resources);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                .authenticationEntryPoint(appOAuth2AuthenticationExceptionEntryPoint)
                .accessDeniedHandler(appAccessDeniedHandler)
                .and()
                .apply(verificationSecurityConfig)
                .and()
                .apply(smsAuthenticationSecurityConfig)
                .and()
                .apply(wxMiniAuthenticationSecurityConfig)
                .and()
                .apply(socialSecurityConfig)
                .and()
                .apply(openIdAuthenticationSecurityConfig)
                .and()
                .csrf().disable();

        authorizationConfigurerManager.config(http.authorizeRequests());
    }
}
