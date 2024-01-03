package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.lexer.token.OperatorType;
import com.googlecode.aviator.runtime.function.seq.SeqReduceFunction;
import com.googlecode.aviator.runtime.function.system.BinaryFunction;
import com.googlecode.aviator.runtime.function.system.TupleFunction;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseVariadicFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class SUM extends BaseVariadicFunction {

    private final TupleFunction tuple = new TupleFunction();
    private final SeqReduceFunction reduce = new SeqReduceFunction();
    private final BinaryFunction ADD = new BinaryFunction(OperatorType.ADD);


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        AviatorObject tuple = this.tuple.variadicCall(env, args);
        return reduce.call(env, tuple, ADD, AviatorNumber.valueOf(0));
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.number;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("SUM函数可以统计输入参数的数值之和，参数V是明细表（子表）的某一个数字字段")
                .usage("SUM(数字1,数字2,...)")
                .example("SUM(10,20,30)，或SUM(V)，V是一个明细内的数字控件，V含有[10,20,30]，则返回60");
    }
}
