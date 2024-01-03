package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.runtime.function.seq.SeqMinFunction;
import com.googlecode.aviator.runtime.function.system.TupleFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.Collector;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseVariadicFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Collection;
import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class MIN extends BaseVariadicFunction {

    private final TupleFunction tuple = new TupleFunction();
    private final SeqMinFunction delegate = new SeqMinFunction();

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length == 1) {
            Object coll1 = args[0].getValue(env);
            Class<?> clazz = coll1.getClass();
            if (Collection.class.isAssignableFrom(clazz) || Collector.class.isAssignableFrom(clazz)) {
                return delegate.call(env, args[0]);
            } else {
                return args[0];
            }
        }

        AviatorObject tuple = this.tuple.variadicCall(env, args);
        return delegate.call(env, tuple);
    }

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
                .desc("MIN函数返回参数列表中的最小值，参数V是明细表（子表）的某一个数字字段")
                .usage("MIN(数字1,数字2,...)")
                .example("MIN(1,2,3)或MIN(V)，V是一个明细内的数字控件，V含有[1,2,3]，则返回1为最小值");
    }
}
