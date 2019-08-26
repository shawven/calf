package com.test.security.social.config;

import com.test.security.social.properties.SocialProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-08-20
 */
@Configuration
@EnableConfigurationProperties(SocialProperties.class)
public class SocialPropertiesConfig {
}
