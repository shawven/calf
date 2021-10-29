package com.github.shawven.calf.examples.oauth2.service;

import org.springframework.aop.framework.AopContext;

/**
 * @author Shoven
 * @date 2019-11-07
 */
public interface AopSupport {

    /**
     * 获取当前的实例对象的Aop代理对象
     * 否则在当前实例对象的方法内部里调用当前实例对象的其他被代理增强的方法会失效，
     * 从而引起未开启事务、缓存穿透等严重问题
     *
     * @param that
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> T aopProxy(T that) {
        return (T) AopContext.currentProxy();
    }
}
