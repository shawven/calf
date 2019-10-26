package com.starter.payment.anotation;

import com.starter.payment.config.GlobalConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(GlobalConfiguration.class)
public @interface EnablePaymentSupport {
}
