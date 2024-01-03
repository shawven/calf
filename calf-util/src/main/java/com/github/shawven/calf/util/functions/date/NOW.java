package com.github.shawven.calf.util.functions.date;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.function.system.DateFormatCache;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class NOW extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        long source = FunctionUtils.getNumberValue(arg1, env).longValue();
        String format = FunctionUtils.getStringValue(arg2, env);
        SimpleDateFormat dateFormat = DateFormatCache.getOrCreateDateFormat(format);

        return AviatorRuntimeJavaType.valueOf(dateFormat.format(new Date(source)));
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.timestamp;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("NOW函数可以返回当前时间（时间戳），精确到毫秒")
                .usage("NOW()")
                .example("NOW()");
    }
}
