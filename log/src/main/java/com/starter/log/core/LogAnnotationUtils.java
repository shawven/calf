package com.starter.log.core;

import com.starter.log.annotation.Log;
import com.starter.log.emun.LogAttribute;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author Shoven
 * @date 2019-07-27 19:23
 */
public class LogAnnotationUtils {


    /**
     * 获取当前方法注解的属性值
     *
     * @param typeClass
     * @param method
     * @param aClass
     * @return
     */
    public static Map<LogAttribute, Object> getAnnotationAttributes(Class typeClass, Method method,
                                                                    Class<? extends Annotation> aClass) {
        // 获取目标类的注解的属性值
        Map<String, Object> classAnnotationAttributes = getAnnotationAttributes(
                getClassLogAnnotation(typeClass, Log.class));

        Annotation methodLogAnnotation;
        // 找不到匹配的注解，当前方法上使用的@Log注解， 通过属性LogType进行调度过来的
        if ((methodLogAnnotation = getMethodLogAnnotation(method, aClass)) == null) {
            methodLogAnnotation = getMethodLogAnnotation(method, Log.class);
        }

        // 获取目标方法的注解的属性值
        Map<String, Object> methodAnnotationAttributes = getAnnotationAttributes(methodLogAnnotation);
        return combineAttributes(methodLogAnnotation, methodAnnotationAttributes, classAnnotationAttributes);
    }



    /**
     * 合并注解属性
     *
     * @param methodLogAnnotation 目标方法的注解
     * @param classAnnotationAttributes 目标类的注解的属性值
     * @param methodAnnotationAttributes 目标方法的注解的属性值
     * @return
     */
    private static Map<LogAttribute, Object> combineAttributes(Annotation methodLogAnnotation,
                                                               Map<String, Object> methodAnnotationAttributes,
                                                               Map<String, Object> classAnnotationAttributes) {
        if (!methodAnnotationAttributes.isEmpty()) {
            Map<LogAttribute, Object> attributes = new HashMap<>();

            for (Map.Entry<String, Object> entry : methodAnnotationAttributes.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                boolean methodAttributeIsBlankString = value != null && "".equals(value.toString());
                boolean classAttributeIsValid = classAnnotationAttributes.get(key) != null;
                if (methodAttributeIsBlankString && classAttributeIsValid) {
                    value = classAnnotationAttributes.get(key);
                }
                attributes.put(LogAttribute.valueOf(key.toUpperCase()), value);
            }

            // @Log的子注解是没有LogType属性的，需要去取元注解@Log的属性
            if (existMetaAnnotation(methodLogAnnotation, Log.class)) {
                attributes.put(LogAttribute.TYPE, getMetaAnnotation(methodLogAnnotation, Log.class).type());
            }
            return attributes;
        }
        return emptyMap();
    }

    /**
     * 获取目标类上的注解
     *
     * @param typeClass
     * @param aClass
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getClassLogAnnotation(Class typeClass, Class<A> aClass) {
        if (typeClass.isAnnotationPresent(aClass)) {
            return (A) typeClass.getAnnotation(aClass);
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
     * 获取元注解
     *
     * @param annotation 注解
     * @param aClass    元注解class
     * @param <A>
     * @return
     */
    private static <A extends Annotation> A getMetaAnnotation(Annotation annotation, Class<A> aClass) {
        if (existMetaAnnotation(annotation, aClass)) {
            return annotation.annotationType().getAnnotation(aClass);
        }
        throw new IllegalArgumentException("找不到此元注解：" + aClass.getSimpleName());
    }


    /**
     * 判断当前注解是否有元注解
     *
     * @param annotation 注解
     * @param aClass     元注解class
     * @param <A>
     * @return
     */
    private static <A extends Annotation> Boolean existMetaAnnotation(Annotation annotation, Class<A> aClass) {
        return annotation != null && annotation.annotationType().isAnnotationPresent(aClass);
    }

    /**
     * 获取注解属性值
     *
     * @param annotation
     * @return
     */
    private static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
        if (annotation == null) {
            return emptyMap();
        }
        return AnnotationUtils.getAnnotationAttributes(annotation, false, true);
    }
}
