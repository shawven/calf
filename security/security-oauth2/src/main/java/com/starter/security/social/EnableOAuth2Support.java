package com.starter.security.social;

import com.starter.security.social.config.OAuth2Configuration;
import com.starter.security.social.config.SocialSupportConfiguration;
import com.starter.security.social.config.SocialConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {SocialSupportConfiguration.class, SocialConfiguration.class, OAuth2Configuration.class})
public @interface EnableOAuth2Support {
}
