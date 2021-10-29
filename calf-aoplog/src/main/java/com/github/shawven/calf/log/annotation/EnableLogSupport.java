package com.github.shawven.calf.log.annotation;

import com.github.shawven.calf.log.config.LogConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LogConfiguration.class)
public @interface EnableLogSupport {
}
