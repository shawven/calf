package com.starter.security.social.annotaion;

import com.starter.security.social.config.SocialSupportConfiguration;
import com.starter.security.social.config.SocialConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {SocialSupportConfiguration.class, SocialConfiguration.class})
public @interface EnableSocialSupport {
}
