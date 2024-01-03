package com.github.shawven.calf.util.functions.logic;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseVariadicFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class XOR extends BaseVariadicFunction {

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        boolean initial = FunctionUtils.getBooleanValue(args[0], env);
        for (int i = 1; i < args.length; i++) {
            if (initial != FunctionUtils.getBooleanValue(args[i], env)) {
                return AviatorBoolean.TRUE;
            }
        }
        return AviatorBoolean.FALSE;
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("XOR函数可以返回所有参数的异或值")
                .usage("XOR(逻辑表达式1,逻辑表达式2,...)")
                .example("XOR(语文成绩>90,数学成绩>90)，如果两门成绩都>90,返回false; 如果两门成绩都<90，返回false; 如果其中一门>90，另外一门<90，返回true");
    }
}
