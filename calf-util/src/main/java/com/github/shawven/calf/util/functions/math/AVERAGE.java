package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.lexer.token.OperatorType;
import com.googlecode.aviator.runtime.function.system.BinaryFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseVariadicFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class AVERAGE extends BaseVariadicFunction {

    private final SUM sum = new SUM();
    private final BinaryFunction delegate = new BinaryFunction(OperatorType.DIV);


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        AviatorObject sum = this.sum.variadicCall(env, args);
        return delegate.call(env, sum, AviatorLong.valueOf(args.length));
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.number;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("AVERAGE函数可以获取一组数值的算术平均值")
                .usage("AVERAGE(数字1,数字2,...)")
                .example("AVERAGE(1,2,3)或AVERAGE(V)，V是一个明细内的数字控件，V含有[1,2,3]，则返回2为平均值");
    }
}
