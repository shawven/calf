package com.github.shawven.calf.util.functions.string;

import com.googlecode.aviator.exception.ExpressionRuntimeException;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class VALUE extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String str = FunctionUtils.getStringValue(arg1, env);
        try {
            Number number = NumberUtils.createNumber(str);
            return AviatorNumber.valueOf(number);
        } catch (Exception e) {
            throw new ExpressionRuntimeException("Cast string to number failed", e);
        }
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.number;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("VALUE函数可以将文本转换为数字")
                .usage("VALUE(文本)")
                .example("VALUE('123')返回123");
    }
}
