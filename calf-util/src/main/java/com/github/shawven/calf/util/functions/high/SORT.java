package com.github.shawven.calf.util.functions.high;

import com.googlecode.aviator.runtime.function.seq.SeqSortFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class SORT extends BaseFunction {

    private final SeqSortFunction delegate = new SeqSortFunction();

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
                .desc("SORT函数用于返回从小到大的自然排序数组")
                .usage("SORT(数组)")
                .example("SORT(V), V是一个明细内的数字控件，V含有[3,1,2]，则返回数组[1,2,3]");
    }
}
