package com.test.controller.base;

import com.test.support.util.ReflectHelpers;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Shoven
 * @date 2019-05-20 8:48
 */
public class QueryParser<T> {

    /**
     * 查询对象
     */
    private Query<T> query;

    private Map<String, Object> equals;

    /**
     * 属性转换器 把属性名称转换成需要的名称  String 对象的属性名称, String 实际设置条件时的名称
     * 比如对象属性名 转 数据库列名
     */
    private Function<String, String> propertyTranslator;

    public QueryParser(Query<T> query) {
        this(query, s -> s);
    }

    public QueryParser(Query<T> query, Function<String, String> propertyTranslator) {
        this.query = query;
        this.propertyTranslator = propertyTranslator;
        this.equals = converterConditions(query.getEq());
    }

    /**
     * 解析升序排序
     *
     * @param orderConsumer
     */
    public void parseOrders(BiConsumer<String, Integer> orderConsumer) {
        parseMapConditions(query.getSort(), orderConsumer);
    }

    /**
     * 解析模糊查询
     *
     * @param likeConsumer
     */
    public void parseLikes(BiConsumer<String, String> likeConsumer) {
        parseMapConditions(query.getLike(), likeConsumer);
    }

    /**
     * 解析大于等于
     *
     * @param greaterEqualConsumer
     */
    public void parseGreaterEqual(BiConsumer<String, String> greaterEqualConsumer) {
        parseMapConditions(query.getGe(), greaterEqualConsumer);
    }

    /**
     * 解析小于等于
     *
     * @param lessEqualConsumer
     */
    public void parseLessEqual(BiConsumer<String, String> lessEqualConsumer) {
        parseMapConditions(query.getLe(), lessEqualConsumer);
    }

    /**
     * 解析的属性和值
     *
     * @param map
     * @param biConsumer
     */
    private <V> void parseMapConditions(Map<String, V> map, BiConsumer<String, V> biConsumer) {
        if (MapUtils.isEmpty(map)) {
            return;
        }
        for (Map.Entry<String, V> entry : map.entrySet()) {
            String key = entry.getKey();
            V value = entry.getValue();
            if (StringUtils.isBlank(key) || !isValidValue(value) || isConflict(key)) {
                continue;
            }
            biConsumer.accept(propertyTranslator.apply(key), value);
        }
    }

    /**
     * 把对象的属性和值转成Map类型的条件
     *
     * @param obj
     * @return
     */
    private Map<String, Object> converterConditions(Object obj) {
        Map<String, Object> properties;
        try {
            properties = ReflectHelpers.objectToMap(obj);
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
        if (properties == null || properties.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<String, Object> conditions = new HashMap<>(properties.size());
        properties.entrySet().stream()
                .filter(entry -> isValidValue(entry.getValue()))
                .forEach(entry -> conditions.put(entry.getKey(), entry.getValue()));
        return conditions;
    }

    /**
     * 是否有效值
     *
     * @param value
     * @return
     */
    private boolean isValidValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return StringUtils.isNotBlank((String) value);
        }
        return true;
    }

    /**
     * 是否有冲突
     * 如果实体设置的属性有值，体属性的条件为等值条件优先于其他
     *
     * @param key
     * @return
     */
    private boolean isConflict(String key)  {
        return isValidValue(equals.get(key));
    }
}
