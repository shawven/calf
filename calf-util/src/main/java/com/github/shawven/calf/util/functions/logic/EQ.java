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
public class EQ extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        return AviatorBoolean.valueOf(arg1.compareEq(arg2, env) == 0);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("EQ函数比较两个值是否相等，相等则返回true，支持文本、数字和日期")
                .usage("EQ(value1,value2)，等价于 value1 == value2")
                .example("EQ(DATE(报名时间), TODAY())选择的报名时间是否是今天，是则返回true");
    }
}
