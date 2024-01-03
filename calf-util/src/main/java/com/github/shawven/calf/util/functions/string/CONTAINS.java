package com.github.shawven.calf.util.functions.string;

import com.googlecode.aviator.runtime.function.string.StringContainsFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class CONTAINS extends BaseFunction {

    private final StringContainsFunction delegate = new StringContainsFunction();

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        return delegate.call(env, arg1, arg2);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("CONCATENATE函数可以将两个或多个文本合并为一个整体")
                .usage("CONCATENATE(文本1,文本2,...)")
                .example("CONCATENATE('123','456')返回123456");
    }
}
