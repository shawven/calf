package com.starter.demo.support.lock;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2020-08-28
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@GlobalLock(tryLock = true)
public @interface GlobalTryLock {

    @AliasFor(value = "value", annotation = GlobalLock.class)
    String value() default "";

    @AliasFor(value = "key", annotation = GlobalLock.class)
    String[] key() default {};

    @AliasFor(value = "timeToExclusive", annotation = GlobalLock.class)
    long timeToExclusive() default 0;
}
