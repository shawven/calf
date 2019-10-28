
package com.starter.security.app.config;

import com.starter.security.base.authentication.configurer.AuthorizationConfigurerManager;
import com.starter.security.social.config.SmsAuthenticationSecurityConfigurer;
import com.starter.security.verification.config.VerificationSecurityConfigurer;
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
public class ResourceServerConfigurer extends ResourceServerConfigurerAdapter {

    @Autowired
    private SmsAuthenticationSecurityConfigurer smsAuthenticationSecurityConfigurer;

    @Autowired
    private OpenIdAuthenticationSecurityConfigurer openIdAuthenticationSecurityConfigurer;

    @Autowired
    private VerificationSecurityConfigurer verificationSecurityConfigurer;

    @Autowired
    private SpringSocialConfigurer socialSecurityConfig;

    @Autowired
    private WxMiniAuthenticationSecurityConfigurer wxMiniAuthenticationSecurityConfigurer;

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
                .apply(verificationSecurityConfigurer)
                .and()
                .apply(smsAuthenticationSecurityConfigurer)
                .and()
                .apply(wxMiniAuthenticationSecurityConfigurer)
                .and()
                .apply(socialSecurityConfig)
                .and()
                .apply(openIdAuthenticationSecurityConfigurer)
                .and()
                .csrf().disable();

        authorizationConfigurerManager.config(http.authorizeRequests());
    }
}
