package com.test.security.browser.config;

import com.test.security.browser.property.BrowserProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-08-20
 */
@Configuration
@EnableConfigurationProperties(BrowserProperties.class)
public class BrowserPropertiesConfig {
}
