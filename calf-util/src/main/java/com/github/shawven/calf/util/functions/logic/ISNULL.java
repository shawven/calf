package com.github.shawven.calf.util.functions.logic;

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
public class ISNULL extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        return AviatorBoolean.valueOf(arg1.getValue(env) == null);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("函数可以用来判断值是否为null")
                .usage("ISEMPTY(文本)")
                .example("ISEMPTY(文本)");
    }
}
