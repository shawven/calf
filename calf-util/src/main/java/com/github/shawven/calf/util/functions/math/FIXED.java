package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class FIXED extends BaseFunction {


    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        int scala = FunctionUtils.getNumberValue(arg2, env).intValue();

        BigDecimal decimal = ((AviatorNumber) arg1).toDecimal(env).setScale(scala, RoundingMode.DOWN);

        return new AviatorString(decimal.toString());
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.string;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("FIXED函数可将数字舍入到指定的小数位数并输出为文本")
                .usage("FIXED(数字,小数位数)")
                .example("FIXED(3.1415,2)返回'3.14'");
    }
}
