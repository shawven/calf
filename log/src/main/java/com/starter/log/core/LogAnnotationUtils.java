package com.starter.log.core;

import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author Shoven
 * @date 2019-07-27 19:23
 */
public class LogAnnotationUtils {

    /**
     * 获取目标类上的注解
     *
     * @param typeClass
     * @param aClass
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getClassLogAnnotation(Class<?> typeClass, Class<A> aClass) {
        if (typeClass.isAnnotationPresent(aClass)) {
            return typeClass.getAnnotation(aClass);
        }
        return null;
    }

    /**
     * 获取目标方法上的注解
     *
     * @param method
     * @param aClass
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getMethodLogAnnotation(Method method, Class<A> aClass) {
        if (method != null && method.isAnnotationPresent(aClass)) {
            return method.getAnnotation(aClass);
        }
        return null;
    }

    /**
     * 获取注解属性值
     *
     * @param annotation
     * @return
     */
    public static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
        if (annotation == null) {
            return emptyMap();
        }
        return AnnotationUtils.getAnnotationAttributes(annotation, false, true);
    }
}
