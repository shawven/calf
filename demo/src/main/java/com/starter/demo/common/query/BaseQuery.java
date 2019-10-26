package com.starter.demo.common.query;

import com.starter.demo.controller.base.PropertyTranslator;
import com.starter.demo.support.util.ReflectHelpers;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Shoven
 * @date 2019-05-23 21:48
 */
@Data
public class BaseQuery<T> {

    /**
     * 页数
     */
    private long size;

    /**
     * 当前页
     */
    private long current;

    /**
     * 偏移量
     */
    private long offset;

    /**
     * 排序字段
     */
    private Map<String, Integer> sort;



    private Long getOffset() {
        return current > 0L ? (current - 1L) * size : 0L;
    }


    /**
     * mybatis里面遍历排序
     */
    protected Map<String, String> sorts;

    /**
     * 属性翻译器
     */
    private PropertyTranslator propertyTranslator;

    public BaseQuery(){
        this.size = 10L;
        this.current = 1L;
        this.offset=0L;
    }

    public Map<String, String> getSorts() {
        if (MapUtils.isEmpty(sorts) && MapUtils.isNotEmpty(sort)) {
            PropertyTranslator propertyTranslator = getPropertyTranslator();
            Map<String, String> sorts = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : sort.entrySet()) {
                String key = propertyTranslator.apply(entry.getKey());
                if (Objects.equals(1, entry.getValue())) {
                    sorts.put(key, "ASC");
                } else {
                    sorts.put(key, "DESC");
                }
            }
            this.sorts = sorts;
            return sorts;
        }
        return this.sorts;
    }

    private PropertyTranslator getPropertyTranslator() {
        if (propertyTranslator == null) {
            propertyTranslator =  new PropertyTranslator(ReflectHelpers.getSuperClassGenericType(getClass(), 0));
        }
        return propertyTranslator;
    }
}
