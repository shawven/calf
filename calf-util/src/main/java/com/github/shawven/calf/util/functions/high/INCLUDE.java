package com.github.shawven.calf.util.functions.high;

import com.googlecode.aviator.runtime.function.seq.SeqIncludeFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class INCLUDE extends BaseFunction {

    private final SeqIncludeFunction delegate = new SeqIncludeFunction();

    public AviatorObject call(final Map<String, Object> env, final AviatorObject arg1,
                              final AviatorObject arg2) {
        return delegate.call(env, arg1, arg2);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("INCLUDE函数用于判断 element 是否在数组中")
                .usage("INCLUDE(数组, element)")
                .example("INCLUDE(V, 3), V是一个明细内的数字控件，V含有[1,2,3]，则返回true");
    }
}
