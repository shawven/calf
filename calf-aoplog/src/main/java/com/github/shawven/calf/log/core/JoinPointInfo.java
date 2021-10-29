package com.github.shawven.calf.log.core;

import com.github.shawven.calf.log.annotation.Log;
import com.github.shawven.calf.log.emun.LogAttribute;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-07-26 9:04
 */

public class JoinPointInfo {

    private final JoinPoint joinPoint;

    public JoinPointInfo(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    /**
     * 提取注解属性值
     *
     * @return
     */
    public Map<LogAttribute, Object> extractAnnotationAttributes() {
        Class<?> typeClass = getTypeClass();
        Method method = getMethod();
        // 获取方法的注解
        Log methodLogAnnotation = LogAnnotationUtils.getMethodLogAnnotation(method, Log.class);
        if (methodLogAnnotation == null) {
            String msg = String.format("Method %s cannot found Log or SubLog annotation", method.getName());
            throw new IllegalStateException(msg);
        }
        // 获取目标类的注解的属性值
        Log classLogAnnotation = LogAnnotationUtils.getClassLogAnnotation(typeClass, Log.class);
        Map<String, Object> classAnnotationAttributes = LogAnnotationUtils.getAnnotationAttributes(classLogAnnotation);

        // 目标方法的注解的属性值
        Map<String, Object> methodAnnotationAttributes = LogAnnotationUtils.getAnnotationAttributes(methodLogAnnotation);

        Map<LogAttribute, Object> attributes = new HashMap<>();
        methodAnnotationAttributes.forEach((key, value) -> {
            // 决定取类注解属性还是方法注解属性
            // 方法注解属性为空且类注解属性有效时取类注解属性，否则取方法注解属性
            boolean methodAttributeIsBlankString = value != null && "".equals(value.toString());
            boolean classAttributeIsValid = classAnnotationAttributes.get(key) != null;
            if (methodAttributeIsBlankString && classAttributeIsValid) {
                value = classAnnotationAttributes.get(key);
            }
            attributes.put(LogAttribute.valueOf(key.toUpperCase()), value);
        });

        // Log注解是子注解（DeleteLog、InsertLog..）的元注解
        // Log的子注解是没有LogType属性的，需要去取元注解@Log的属性, 获取方法注解的元注解（Log）
        // 判断当前注解是否有元注解
        Log metaAnnotation = null;
        if (methodLogAnnotation.annotationType().isAnnotationPresent(Log.class)) {
            metaAnnotation =  methodLogAnnotation.annotationType().getAnnotation(Log.class);
        }
        // 存在元注解则获取元注解的logType,不存在元注解，说明此方法本身注解就是Log注解
        if (metaAnnotation != null) {
            attributes.put(LogAttribute.TYPE, metaAnnotation.type());
        }
        return attributes;
    }


    public Class<?> getTypeClass() {
        return joinPoint.getTarget().getClass();
    }

    public Method getMethod() {
        return ((MethodSignature) joinPoint.getSignature()).getMethod();
    }
}
