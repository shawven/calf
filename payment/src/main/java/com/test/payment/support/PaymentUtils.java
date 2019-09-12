package com.test.payment.support;

import com.alipay.api.internal.util.file.StringBuilderWriter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Collections.emptyMap;

/**
 * @author Shoven
 * @date 2019-09-03
 */
public class PaymentUtils {

    public static Map<String, String> parseParameterMap(Map<String, ?> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        for (String name : parameterMap.keySet()) {
            Object value = parameterMap.get(name);
            String valueStr = "";
            if (value instanceof String[]) {
                String[] values = (String[]) value;
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
            } else {
                valueStr = value != null ? value.toString() : "";
            }
            params.put(name, valueStr);
        }
        return params;
    }

    public static String read(InputStream inputStream){
        StringBuilderWriter writer = new StringBuilderWriter();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        char[] buf = new char[4096];
        int n;
        try {
            while (-1 != (n = reader.read(buf))) {
                writer.write(buf, 0, n);
            }
        } catch (IOException e) {
            return null;
        }
        return writer.toString();
    }

    public static Map<String, String> readUrlParamsToMap(InputStream inputStream){
        return splitPairString(read(inputStream));
    }

    public static Map<String, String> splitPairString(String pairString) {
        if (pairString == null || pairString.length() == 0) {
            return emptyMap();
        }
        String[] keyValuePairs = pairString.split("&");
        if (keyValuePairs.length == 0) {
            return emptyMap();
        }
        LinkedHashMap<String, String> newPairs = new LinkedHashMap<>();
        for (String keyValuePair : keyValuePairs) {
            String[] elements = keyValuePair.split("=", 2);
            if (elements.length != 2) {
                continue;
            }
            try {
                String decode = URLDecoder.decode(elements[1], "UTF-8");
                newPairs.put(elements[0], decode);
            } catch (UnsupportedEncodingException ignored) {}
        }
        return newPairs;
    }

    public static <T> Class<T> getSuperClassGenericType(Class cls, int index) {
        String simpleName = cls.getSimpleName();
        Type genType = cls.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            throw new RuntimeException(String.format("%s's superclass not ParameterizedType", simpleName));
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                if (!(params[index] instanceof Class)) {
                    throw new RuntimeException(String.format("%s not set the actual class on" +
                            " superclass generic parameter", simpleName));
                } else {
                    return (Class<T>) params[index];
                }
            } else {
                throw new RuntimeException(String.format("Warn: Index: %s, Size of %s's ParameterizedType: %s .",
                        index, cls.getSimpleName(), params.length));
            }
        }
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return obj.toString();
        }

        Class<?> cls = obj.getClass();
        StringBuilder builder = new StringBuilder()
                .append(cls.getSimpleName()).append("{");

        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(cls, Object.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        PropertyDescriptor[] all = info.getPropertyDescriptors();
        try {
            for (PropertyDescriptor pd : all) {
                Method readMethod = pd.getReadMethod();
                if (readMethod != null) {
                    Object result = readMethod.invoke(obj);
                    builder.append(pd.getName()).append(" = ").append(result).append(",");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
        } catch (Exception ignored) { }

        builder.append("}");
        return builder.toString();
    }

    public static boolean isBlankString(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }
}
