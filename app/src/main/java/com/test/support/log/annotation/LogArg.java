package com.test.support.log.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-07-25 16:16
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogArg {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
