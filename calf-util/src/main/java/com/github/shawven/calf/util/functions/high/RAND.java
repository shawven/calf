package com.github.shawven.calf.util.functions.high;

import com.googlecode.aviator.runtime.function.system.RandomFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class RAND extends BaseFunction {

    private final RandomFunction delegate = new RandomFunction();

    @Override
    public AviatorObject call(Map<String, Object> env) {
        return delegate.call(env);
    }


    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg) {
        return delegate.call(env, arg);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.number;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("RAND函数用于返回一个随机数")
                .usage("RAND(), RAND([n])")
                .example("RAND()返回一个介于[0, 1)的随机数结果为小数，rand(10)返回一个介于[0, 10)的随机数结果为整数");
    }
}
