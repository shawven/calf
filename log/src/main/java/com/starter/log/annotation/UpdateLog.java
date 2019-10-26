package com.starter.log.annotation;

import com.starter.log.emun.LogType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-07-25 16:17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Log(type = LogType.UPDATE)
@Inherited
@Documented
public @interface UpdateLog {

    @AliasFor("message")
    String value() default "";

    @AliasFor("value")
    String message() default "";

    String module() default "";
}