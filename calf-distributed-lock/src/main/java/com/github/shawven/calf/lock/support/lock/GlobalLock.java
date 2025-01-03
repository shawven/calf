package com.github.shawven.calf.lock.support.lock;

import java.lang.annotation.*;

/**
 * 实际的锁住的资源key为 (value + key)，如果未设置key的话只有value生效则所有线程抢同一把锁
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
     * lock 运行时key, 支持SpEL表达式,
     *
     */
    String[] key() default {};

    /**
     * 尝试获取锁，
     *  true：获取锁成功则进入方法，获取锁失败则不进入方法
     *  false：一直等待到获取锁
     */
    boolean tryLock() default false;

    /**
     * 锁等待时间，tryLock生效
     */
    long waitTime() default -1;

    /**
     * 锁租用时间，watchdog会自动续期
     */
    long leaseTime() default -1;

    /**
     * 以下情况触发降级方法，无降级方法时会抛出异常！
     * 1、tryLock获取锁失败
     * 2、tryLock和lock获取锁被中断
     */
    String fallback() default "";

    /**
     * 释放锁，同时设置leaseTime可以达到释放锁之后任然占有该锁xx时间的效果
     */
    boolean release() default true;
}
