package com.github.shawven.calf.util.functions.logic;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class IF extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        return arg1.booleanValue(env) ? arg2 : arg3;
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.generic;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("IF函数判断一个条件能否满足;如果满足返回一个值，如果不满足则返回另外一个值")
                .usage("IF(逻辑表达式,为true时返回的值,为false时返回的")
                .example("IF(语文成绩>60,'及格','不及格')，当地理成绩>60时返回及格，否则返回不及格");
    }
}
