package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.runtime.function.math.MathSqrtFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class SQRT extends BaseFunction {

    private final MathSqrtFunction delegate = new MathSqrtFunction();

    @Override
    public AviatorObject call(final Map<String, Object> env, final AviatorObject arg1) {
        return delegate.call(env, arg1);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.number;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("SQRT函数可以获取一个数字的正平方根")
                .usage("SQRT(数字)")
                .example("SQRT(9)返回3，也就是9的正平方根");
    }
}
