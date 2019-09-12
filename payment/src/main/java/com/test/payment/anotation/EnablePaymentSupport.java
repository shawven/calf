package com.test.payment.anotation;

import com.test.payment.config.PaymentConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PaymentConfiguration.class)
public @interface EnablePaymentSupport {
}
