package com.test.security.validate.config;

import com.test.security.validate.property.ValidationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Configuration
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationPropertiesConfig {
}
