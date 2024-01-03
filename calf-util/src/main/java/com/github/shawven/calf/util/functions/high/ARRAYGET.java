package com.github.shawven.calf.util.functions.high;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.function.seq.SeqGetFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class ARRAYGET extends BaseFunction {

    private final SeqGetFunction delegate = new SeqGetFunction();

    public AviatorObject call(final Map<String, Object> env, final AviatorObject arg1,
                              final AviatorObject arg2) {
        int index = FunctionUtils.getNumberValue(arg2, env).intValue();
        if (index <= 0) {
            return AviatorNil.NIL;
        }
        // 从1 开始
        return delegate.call(env, arg1, AviatorNumber.valueOf(index - 1));
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.generic;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("ARRAYGET函数用于获取数据集中第k个值用法: ARRAYGET(array,k)")
                .usage("ARRAYGET(数组，k)")
                .example("ARRAYGET(V，3), V是一个明细内的数字控件，返回明细'数字'中第3个的'数字'");
    }
}
