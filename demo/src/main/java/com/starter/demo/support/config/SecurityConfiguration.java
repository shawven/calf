package com.starter.demo.support.config;

import com.github.shawven.security.authorization.AuthorizationConfigureProvider;
import com.starter.demo.service.UserService;
import com.starter.demo.support.handler.LoginFailureHandler;
import com.starter.demo.support.handler.LoginSuccessHandler;
import com.starter.demo.support.UserDetailsServiceImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Shoven
 * @date 2019-10-26
 */
@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean({"userDetailsService", "phoneUserDetailsService", "socialUserDetailService"})
    public UserDetailsServiceImpl userDetailsService(UserService userService) {
        return new UserDetailsServiceImpl(userService);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler(ApplicationEventPublisher eventPublisher, UserService userService) {
        return new LoginSuccessHandler(eventPublisher, userService);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler(ApplicationEventPublisher eventPublisher) {
        return new LoginFailureHandler(eventPublisher);
    }

    @Bean
    public AuthorizationConfigureProvider authorizationConfigurerProvider() {
        return new AuthorizationConfigureProvider() {
            @Override
            public void config(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config) {
                try {
                    config.and().headers().frameOptions().disable();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                config.anyRequest().access("@rbacService.hasPermission(request, authentication)");
            }

            @Override
            public boolean isAnyRequest() {
                return true;
            }
        };
    }
}
