package com.starter.demo.support.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;

/**
 * 反射助手
 *
 * @author Shoven
 * @date 2018-10-12 15:24
 */
public class ReflectHelpers {

    /**
     * 对象值转换
     *
     * @param obj 对象
     */
    public static void map(Object obj, Function<Object, Object> mapper) throws Exception {
        if (obj == null) {
            return;
        }
        PropertyDescriptor[] pds = getPropertyDescriptors(obj.getClass(), true, false);
        for (PropertyDescriptor pd : pds) {
            Method writeMethod = pd.getWriteMethod();
            Object result = pd.getReadMethod().invoke(obj);
            writeMethod.invoke(obj, mapper.apply(result));
        }
    }

    /**
     * Map 转 Object
     *
     * @param map map
     * @param cls 对象Class
     * @param <T> 对象的参数类型
     * @return 对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> cls) throws Exception {
        if (map == null || map.isEmpty()) {
            return null;
        }
        T obj = cls.newInstance();
        PropertyDescriptor[] pds = getPropertyDescriptors(cls, true, false);
        for (PropertyDescriptor pd : pds) {
            pd.getWriteMethod().invoke(obj, map.get(pd.getName()));
        }
        return obj;
    }

    /**
     * Object 转 Map
     *
     * @param obj 对象
     * @return 对象属性Map
     */
    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return emptyMap();
        }
        PropertyDescriptor[] pds = getPropertyDescriptors(obj.getClass(), false, true);
        Map<String, Object> map = new HashMap<>(pds.length);
        for (PropertyDescriptor pd : pds) {
            String key = pd.getName();
            Object value = pd.getReadMethod().invoke(obj, map.get(pd.getName()));
            map.put(key, value);
        }
        return map;
    }

    /**
     * set对象的属性
     *
     * @param obj   对象
     * @param name  属性名
     * @param value 值
     */
    public static void setProperty(Object obj, String name, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        setAccessible(field);
        field.set(obj, value);
    }


    /**
     * get对象的属性
     *
     * @param obj  对象
     * @param name 属性名
     */
    public static Object getProperty(Object obj, String name) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        setAccessible(field);
        return field.get(obj);
    }


    /**
     * 查找类上使用了该注解的属性名称
     *
     * @param cls   对象Class
     * @param aClass 注解Class
     * @return 属性数组
     */
    @SuppressWarnings("rawtypes")
    public static Field[] getPropertyNameByAnnotation(Class cls, Class<? extends Annotation> aClass) {
        Field[] declaredFields = cls.getDeclaredFields();
        List<Field> fields = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(aClass)) {
                fields.add(declaredField);
            }
        }
        return fields.toArray(new Field[0]);
    }

    /**
     * 获取父类参数类型
     *
     * @param cls 子类
     * @param index 父类的参数类型索引
     * @param <T> 参数类型
     * @return 参数类型Class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getSuperClassGenericType(Class<?> cls, int index) {
        String simpleName = cls.getSimpleName();
        Type genType = cls.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(String.format("%s's superclass not ParameterizedType", simpleName));
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                if (!(params[index] instanceof Class)) {
                    throw new IllegalArgumentException(String.format("%s not set the actual class on" +
                            " superclass generic parameter", simpleName));
                } else {
                    return (Class<T>) params[index];
                }
            } else {
                throw new IndexOutOfBoundsException(String.format("Warn: Index: %s, Size of %s's ParameterizedType: %s .",
                        index, cls.getSimpleName(), params.length));
            }
        }
    }

    /**
     * 获取接口参数类型
     *
     * @param cls 实现类
     * @param index 接口的参数类型索引
     * @param <T> 参数类型
     * @return 参数类型Class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getInterfaceGenericType(Class<?> cls, int index) {
        String simpleName = cls.getSimpleName();
        Type[] genericInterfaces = cls.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genericInterface).getActualTypeArguments();
                if (index < params.length && index >= 0) {
                    if (params[index] instanceof Class) {
                        return (Class<T>) params[index];
                    }
                }
            }
        }
        throw new IllegalArgumentException(String.format("Class %s not ParameterizedType from interfaces ", simpleName));
    }

    /**
     * 父类是否存在此参数类型
     *
     * @param cls 子类
     * @param needle 待查找的参数类型
     * @return 查找结果
     */
    public static boolean existSuperClassGenericType(Class<?> cls, Class<?> needle) {
        Type genType = cls.getGenericSuperclass();
        if (genType instanceof ParameterizedType) {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (params == null || params.length == 0) {
                return false;
            }
            for (Type param : params) {
                if (param instanceof Class && ((Class<?>) param).isAssignableFrom(needle)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 当前类的接口是否存在此参数类型
     *
     * @param cls 实现类
     * @param needle 待查找的参数类型
     * @return 查找结果
     */
    public static boolean existInterfaceGenericType(Class<?> cls, Class<?> needle) {
        Type[] genericInterfaces = cls.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genericInterface).getActualTypeArguments();
                if (params == null || params.length == 0) {
                    return false;
                }
                for (Type param : params) {
                    if (param instanceof Class && ((Class<?>) param).isAssignableFrom(needle)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void setAccessible(Field field) {
        boolean unaccessible = !Modifier.isPublic(field.getModifiers())
                || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                || Modifier.isFinal(field.getModifiers());
        if (unaccessible && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    private static PropertyDescriptor[] getPropertyDescriptors(Class<?> type, boolean read, boolean write) {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(type, Object.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        PropertyDescriptor[] all = info.getPropertyDescriptors();
        if (read && write) {
            return all;
        } else {
            List<PropertyDescriptor> properties = new ArrayList<>(all.length);
            for (PropertyDescriptor pd : all) {
                if (read && pd.getReadMethod() != null || write && pd.getWriteMethod() != null) {
                    properties.add(pd);
                }
            }
            return properties.toArray(new PropertyDescriptor[0]);
        }
    }
}
