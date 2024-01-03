package com.github.shawven.calf.util.functions;

import lombok.Data;

import java.util.Map;

/**
 * 公式评估请求
 *
 * @author xw
 * @date 2023/11/24
 */
@Data
public class FormulaEvaluationRequest {

    /**
     * 表达式
     */
    private String expression;

    /**
     * 参数
     */
    private Map<String, Object> args;

    /**
     * 返回值类型
     */
    private FormulaTypeEnum returnType;

    public FormulaEvaluationRequest(String expression, Map<String, Object> args, FormulaTypeEnum returnType) {
        this.expression = expression;
        this.args = args;
        this.returnType = returnType;
    }
}
