package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.runtime.function.math.MathPowFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class POW extends BaseFunction {

    private final MathPowFunction delegate = new MathPowFunction();

    @Override
    public AviatorObject call(final Map<String, Object> env, final AviatorObject arg1, final AviatorObject arg2) {
        return delegate.call(env, arg1, arg2);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.number;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("POWER函数可以获取数字乘幂的结果")
                .usage("POWER(数字,指数)")
                .example("POWER(3,2)返回9，也就是3的2次方");
    }
}
