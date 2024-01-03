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
public class OR extends BaseVariadicFunction {

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        boolean result = FunctionUtils.getBooleanValue(args[0], env);
        for (int i = 1; i < args.length; i++) {
            result |= FunctionUtils.getBooleanValue(args[i], env);
            if (result) {
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
                .desc("如果任意参数为真，OR 函数返回布尔值true; 如果所有参数为假，返回布尔值")
                .usage("OR(逻辑表达式1,逻辑表达式2,...)")
                .example("OR(地理成绩>90,历史成绩>90,政治成绩>90)，任何一门课成绩>90，返回true，否则返回false");
    }
}
