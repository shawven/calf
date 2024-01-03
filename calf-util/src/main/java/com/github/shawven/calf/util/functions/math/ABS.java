package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.runtime.function.math.MathAbsFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class ABS extends BaseFunction {

    private final MathAbsFunction delegate = new MathAbsFunction();

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
                .desc("ABS函数可以获取一个数的绝对值")
                .usage("ABS(数字)")
                .example("ABS(-8)可以返回8，也就是-8的绝对值");
    }
}
