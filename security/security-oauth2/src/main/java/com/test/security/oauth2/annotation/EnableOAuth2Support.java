package com.test.security.oauth2.annotation;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableOAuth2Support {
}
