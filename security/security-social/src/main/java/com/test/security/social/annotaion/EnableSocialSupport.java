package com.test.security.social.annotaion;

import com.test.security.social.config.SocialConfiguration;
import com.test.security.social.config.SocialSupportConfiguration;
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
