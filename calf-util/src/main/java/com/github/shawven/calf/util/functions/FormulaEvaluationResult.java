package com.github.shawven.calf.util.functions;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xw
 * @date 2023/11/27
 */
@Data
public class FormulaEvaluationResult implements Serializable {

    /**
     * 是否成功，失败时返回值类型默认结果
     * @see FormulaTypeEnum
     */
    private boolean success;

    /**
     * 成功值 或者 默认值
     */
    private Object value;

    /**
     * 代码
     */
    private int code;

    /**
     * 描述
     */
    private String error;


    public FormulaEvaluationResult(boolean success, Object value, int code, String error) {
        this.success = success;
        this.value = value;
        this.code = code;
        this.error = error;
    }

    public static FormulaEvaluationResult success(Object value) {
        return new FormulaEvaluationResult(true, value, 0, null);
    }

    public static FormulaEvaluationResult error(Object value, int code, String msg) {
        return new FormulaEvaluationResult(false, value, code, msg);
    }
}
