package com.github.shawven.calf.util.functions.math;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class ROUND extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        int scala = FunctionUtils.getNumberValue(arg2, env).intValue();

        BigDecimal decimal = ((AviatorNumber) arg1).toDecimal(env).setScale(scala, RoundingMode.HALF_UP);

        return AviatorLong.valueOf(decimal.doubleValue());
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.number;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("ROUND函数可以将数字四舍五入到指定的位数")
                .usage("ROUND(数字,数字位数)")
                .example("ROUND(3.1485,2)返回3.15");
    }
}
