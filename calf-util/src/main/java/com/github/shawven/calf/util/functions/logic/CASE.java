package com.github.shawven.calf.util.functions.logic;

import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseVariadicFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class CASE extends BaseVariadicFunction {

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        int n = args.length;
        if (n % 2 != 0) {
            throw new IllegalArgumentException("Wrong number of args (" + n + ") passed to: " + getName());
        }
        for (int i = 0; i < n; i += 2) {
            if (args[i].booleanValue(env)) {
                return AviatorRuntimeJavaType.valueOf(args[i + 1].getValue(env));
            }
        }
        return AviatorNil.NIL;
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.generic;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("IF判断是否满足一个或多个条件，且返回符合第一个TRUE条件的值，CASE可以取代多个IF语句嵌套")
                .usage("CASE(条件表达式1,条件表达式1为true返回该值,条件表达式2,条件表达式2为true返回该值,...)")
                .example("CASE(语文成绩>=60,及格,语文成绩>90,优秀),当语文成绩>90时，返回及格，因为第一条表达式就满足了，所以就返回该表达式为真的值");
    }
}
