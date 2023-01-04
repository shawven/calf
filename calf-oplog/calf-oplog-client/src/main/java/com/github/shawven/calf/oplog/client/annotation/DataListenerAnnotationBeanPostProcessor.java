package com.github.shawven.calf.oplog.client.annotation;

import com.github.shawven.calf.oplog.client.DataSubscribeHandler;
import com.github.shawven.calf.oplog.client.DataSubscriberMethodAdapter;
import com.github.shawven.calf.oplog.client.DataSubscribeRegistry;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;


public class DataListenerAnnotationBeanPostProcessor implements SmartInitializingSingleton, BeanPostProcessor {

    private DataSubscribeRegistry dataSubscribeRegistry;

    public void setDataListenerRegistry(DataSubscribeRegistry dataSubscribeRegistry) {
        this.dataSubscribeRegistry = dataSubscribeRegistry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Assert.state(dataSubscribeRegistry != null, "dataListenerRegistry must be set");

    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Map<Method, Set<DataSubscriber>> annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                (MethodIntrospector.MetadataLookup<Set<DataSubscriber>>) method -> {
                    Set<DataSubscriber> dataSubscribers = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                            method, DataSubscriber.class);
                    return (!dataSubscribers.isEmpty() ? dataSubscribers : null);
                });
        // 处理注解方法
        if (!annotatedMethods.isEmpty()) {
            annotatedMethods.forEach((method, annotations) -> annotations.forEach(ann -> {
                DataSubscriberMethodAdapter methodAdapter = new DataSubscriberMethodAdapter(bean, method, ann);
                dataSubscribeRegistry.registerHandler(methodAdapter);
            }));
        } else {
            if (DataSubscribeHandler.class.isAssignableFrom(AopUtils.getTargetClass(bean))) {
                dataSubscribeRegistry.registerHandler((DataSubscribeHandler) bean);
            }
        }

        return bean;
    }
}
