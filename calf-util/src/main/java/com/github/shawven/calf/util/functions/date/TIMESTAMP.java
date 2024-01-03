package com.github.shawven.calf.util.functions.date;

import com.googlecode.aviator.exception.ExpressionRuntimeException;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.function.system.DateFormatCache;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class TIMESTAMP extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String source = FunctionUtils.getStringValue(arg1, env);
        String format = FunctionUtils.getStringValue(arg2, env);
        SimpleDateFormat dateFormat = DateFormatCache.getOrCreateDateFormat(format);
        try {
            return AviatorRuntimeJavaType.valueOf(dateFormat.parse(source).getTime());
        } catch (ParseException e) {
            throw new ExpressionRuntimeException("Cast string to date failed", e);
        }
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.timestamp;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("TIMESTAMP函数可以将时间转换为时间戳")
                .usage("TIMESTAMP(时间)")
                .example("TIMESTAMP('2015-11-1')返回1446307200000");
    }
}
