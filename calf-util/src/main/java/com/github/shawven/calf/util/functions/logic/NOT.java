package com.github.shawven.calf.util.functions.logic;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class NOT extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        return AviatorBoolean.valueOf(!FunctionUtils.getBooleanValue(arg1, env));
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("NOT函数返回与指定表达式相反的布尔值")
                .usage("NOT(逻辑表达式)")
                .example("NOT(数学成绩>60)，如果数学成绩大于60返回false，否则返回true");
    }
}
