package com.starter.security.base.config;

import com.starter.security.base.authentication.configurer.AuthorizationConfigurerManager;
import com.starter.security.base.authentication.configurer.AuthorizationConfigurerProvider;
import com.starter.security.base.properties.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-10-28
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration {

    @Bean
    public AuthorizationConfigurerManager authorizationConfigurerManager(SecurityProperties securityProperties,
            List<AuthorizationConfigurerProvider> authorizationConfigurerProviders) {
        return new AuthorizationConfigurerManager(authorizationConfigurerProviders, securityProperties);
    }
}
