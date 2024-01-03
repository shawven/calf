package com.github.shawven.calf.util.functions;

import java.util.Collections;

/**
 * @author xw
 * @date 2023/11/21
 */
public enum FormulaTypeEnum {

    /**
     * 文本
     */
    string(""),

    /**
     * 数字
     */
    number(0),

    /**
     * 时间戳
     */
    timestamp(0),

    /**
     * 布尔
     */
    bool(false),

    /**
     * 数组
     */
    array(Collections.emptyList()),

    /**
     * 对象
     */
    object(new Object()),

    /**
     * 泛型
     */
    generic(null);

    private final Object defaultValue;

    FormulaTypeEnum(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
