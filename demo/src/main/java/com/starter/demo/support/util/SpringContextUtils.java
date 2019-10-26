package com.starter.demo.support.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;

/**
 * @author Shoven
 * @date 2018-09-30 10:32
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContextUtils.context = Objects.requireNonNull(context);
    }

    /**
     * 根据参数类型，从被注解标注的bean对象中查找
     *
     * @param cls     bean中含有的参数类型
     * @param aClass  注解Class
     * @param <T>     注解
     * @return        bean
     */
    public static <T> T getBeanByParameterizedType(Class cls, Class<? extends Annotation> aClass) {
        Map<String, Object> beans = getContext().getBeansWithAnnotation(aClass);
        for (Object bean : beans.values()) {
            Object target;
            try {
                target = AopProxyUtils.getTarget(bean);
            } catch (Exception e) {
                continue;
            }
            Class targetClass = target.getClass();
            if (ReflectHelpers.existSuperClassGenericType(targetClass, cls)) {
                //noinspection unchecked
                return (T)bean;
            }
            if (ReflectHelpers.existInterfaceGenericType(targetClass, cls)) {
                //noinspection unchecked
                return (T)bean;
            }
        }
        throw new RuntimeException("None of the Bean's ParameterizedType is " + cls.getSimpleName());
    }



    /**
     * 通过name取得Bean, 自动转型为所赋值对象的类型 该类型的bean在IOC容器中也必须是唯一的
     *
     * @param name bean的name或id
     * @param <T> 要加载的Bean的类型
     * @return Bean
     */
    public static <T> T getBean(String name) {
        //noinspection unchecked
        return (T) getContext().getBean(name);
    }

    /**
     * 通过class取得Bean,该类型的bean在IOC容器中也必须是唯一的
     *
     * @param cls 要加载的Bean的class
     * @param <T> 要加载的Bean的类型
     * @return Bean
     */
    public static <T> T getBean(Class<T> cls) {
        return getContext().getBean(cls);
    }

    /**
     * 通过name和class取得bean，比较适合当类型不唯一时，再通过id或者name来获取bean
     *
     * @param name bean的name或id
     * @param cls 要加载的Bean的class
     * @param <T> 要加载的Bean的类型
     * @return Bean
     */
    public static <T> T getBean(String name, Class<T> cls) {
        return getContext().getBean(name, cls);
    }


    /**
     * 取得存储在静态变量中的ApplicationContext.
     */
    public static ApplicationContext getContext() {
        Assert.notNull(context, "context 未注入");
        return context;
    }
}
