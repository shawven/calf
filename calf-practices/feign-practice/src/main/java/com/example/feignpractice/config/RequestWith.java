package com.example.feignpractice.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 每次请求都携带的头部或url参数
 * @author kingdee
 */
@Target({ ElementType.METHOD,ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestWith {

    /**
     * 头部
     */
	Dict[] headers() default {};

    /**
     * url参数
     */
	Dict[] queries() default {};

    /**
     * 默认值时不生效
     * 局部配置 > 配置类 > 全局配置
     *
     * @return
     */
    int connectTimeoutMillis() default 0;

    /**
     * @return
     */
    int readTimeoutMillis() default 0;
}
