package com.github.shawven.calf.util.functions.high;

import com.googlecode.aviator.runtime.function.system.TupleFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseVariadicFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class ARRAY extends BaseVariadicFunction {

    private final TupleFunction delegate = new TupleFunction();

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        return delegate.variadicCall(env, args);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.array;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("ARRAY函数可以返回一个数组对象")
                .usage("ARRAY(x, y, ...)")
                .example("ARRAY('语文成绩', '数学成绩', '英语成绩')");
    }
}
