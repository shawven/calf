
package com.test.security.oauth2.config;

import com.test.security.oauth2.property.OAuth2Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(OAuth2Properties.class)
public class OAuth2PropertiesConfig {

}
