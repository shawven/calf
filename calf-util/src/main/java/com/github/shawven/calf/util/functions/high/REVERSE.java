package com.github.shawven.calf.util.functions.high;

import com.googlecode.aviator.runtime.function.seq.SeqReverseFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class REVERSE extends BaseFunction {

    private final SeqReverseFunction delegate = SeqReverseFunction.INSTANCE;

    public AviatorObject call(final Map<String, Object> env, final AviatorObject arg1) {
        return delegate.call(env, arg1);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.array;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("REVERSE函数用于返回逆序数组")
                .usage("REVERSE(数组)")
                .example("REVERSE(V), V是一个明细内的数字控件，V含有[1,2,3]，则返回数组[3,2,1]");
    }
}
