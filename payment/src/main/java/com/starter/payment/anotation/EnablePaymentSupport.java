package com.starter.payment.anotation;

import com.starter.payment.config.AppConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AppConfiguration.class)
public @interface EnablePaymentSupport {
}
