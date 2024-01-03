package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.runtime.function.seq.SeqMaxFunction;
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
public class MAX extends BaseVariadicFunction {

    private final TupleFunction tuple = new TupleFunction();
    private final SeqMaxFunction delegate = new SeqMaxFunction();

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
                .desc("MAX函数可以获取一组数值的最大值")
                .usage("MAX(数字1,数字2,...)")
                .example("MAX(1,2,3)或MAX(V)，V是一个明细内的数字控件，，V含有[1,2,3]，则返回3为最大值");
    }
}
