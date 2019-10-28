package com.starter.security.social;

import com.starter.security.social.config.OAuth2Configuration;
import com.starter.security.social.config.SocialConfiguration;
import com.starter.security.social.config.SocialSecurityConfigurer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {SocialConfiguration.class, SocialSecurityConfigurer.class, OAuth2Configuration.class})
public @interface EnableOAuth2Support {
}
