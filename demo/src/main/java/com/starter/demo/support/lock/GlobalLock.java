package com.starter.demo.support.lock;

import java.lang.annotation.*;

/**
 * 实际的锁住的资源key为 (value + key)，如果没有设置key的话为全局锁，
 *
 * @author Shoven
 * @date 2020-08-28
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GlobalLock {

    /**
     * lock 资源名称，默认为类和方法名的组合
     */
    String value() default "";

    /**
     * lock 运行时key, SpEL表达式,
     *
     */
    String[] key() default {};

    /**
     * 尝试获取锁
     */
    boolean tryLock() default false;

    /**
     * 锁独占持续时间（默认立即释放）
     */
    long timeToExclusive() default 0;
}
