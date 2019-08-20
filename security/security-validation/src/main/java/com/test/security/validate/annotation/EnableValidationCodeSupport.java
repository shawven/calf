package com.test.security.validate.annotation;

import com.test.security.validate.config.ValidationSupportConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ValidationSupportConfiguration.class)
public @interface EnableValidationCodeSupport {
}
