package com.starter.security.app.config;

import com.starter.security.app.RedisVerificationRepository;
import com.starter.security.app.authentication.AppAccessDeniedHandler;
import com.starter.security.app.authentication.AppAuthenticationFailureHandler;
import com.starter.security.app.authentication.AppAuthenticationSuccessHandler;
import com.starter.security.app.authentication.AppOAuth2AuthenticationExceptionEntryPoint;
import com.starter.security.app.oauth2.ClientAuthenticationFilter;
import com.starter.security.base.authentication.configurer.AuthorizationConfigurerProvider;
import com.starter.security.social.properties.SocialProperties;
import com.starter.security.verification.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.Filter;

/**
 * @author Shoven
 * @date 2019-08-20
 */
@Configuration
public class AppConfiguration {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private SocialProperties socialProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnMissingBean
    public VerificationRepository redisVerificationRepository() {
        return new RedisVerificationRepository(redisTemplate);
    }

    /**
     * 验证成功处理器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationSuccessHandler appAuthenticationSuccessHandler(ClientDetailsService clientDetailsService,
                                                                            AuthorizationServerTokenServices services) {
        return new AppAuthenticationSuccessHandler(clientDetailsService, passwordEncoder, services);
    }

    /**
     * 验证失败处理器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationFailureHandler appAuthenticationFailureHandler() {
        return new AppAuthenticationFailureHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public AccessDeniedHandler appAccessDeniedHandler() {
        return new AppAccessDeniedHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationEntryPoint appOAuth2AuthenticationExceptionEntryPoint() {
        return new AppOAuth2AuthenticationExceptionEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationConfigurerProvider appAuthorizationConfigurerProvider() {
        return new AppAuthorizationConfigurerProvider(socialProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public Filter clientAuthenticationFilter(ClientDetailsService clientDetailsService) {
        return new ClientAuthenticationFilter(clientDetailsService, passwordEncoder);
    }
}
