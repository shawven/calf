package com.starter.demo.support.util;

import org.springframework.cglib.beans.BeanCopier;

import static java.util.Objects.requireNonNull;

/**
 * @author Shoven
 * @date 2019-08-01 11:10
 */
public class BeanCopierUtils {

    /**
     * ben属性复制
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        requireNonNull(source, "源对象不能为空");
        requireNonNull(source, "目标对象不能为空");
        try {
            BeanCopier beanCopier = BeanCopier.create(source.getClass(), target.getClass(), false);
            beanCopier.copy(source, target, null);
        } catch (Exception e) {
            throw new RuntimeException("复制对象失败：" + e.getMessage(), e);
        }
    }
}
