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
public class LE extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        return AviatorBoolean.valueOf(arg1.compare(arg2, env) <= 0);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("LE函数用于比较两个数的大小，value1小于等于value2返回true")
                .usage("LE(value1,value2)，等价于 value1 <= value2")
                .example("示例: GE(年龄，30) 年龄小于等于30的返回true");
    }
}
