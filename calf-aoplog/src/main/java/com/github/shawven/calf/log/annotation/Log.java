package com.github.shawven.calf.log.annotation;

import com.github.shawven.calf.log.emun.LogType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * @author Shoven
 * @date 2019-07-25 16:17
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Log {

    @AliasFor("message")
    String value() default "";

    @AliasFor("value")
    String message() default "";

    String module() default "";

    LogType type() default LogType.COMMON;
}
