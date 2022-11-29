package com.example.nativepractice.util;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

public class Spring {
	private static ApplicationContext context;

	public static void init(ApplicationContext ctx) {
        context = ctx;
	}


    /**
     * 通过name取得Bean, 自动转型为所赋值对象的类型 该类型的bean在IOC容器中也必须是唯一的
     *
     * @param name bean的name或id
     * @param <T> 要加载的Bean的类型
     * @return Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
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
