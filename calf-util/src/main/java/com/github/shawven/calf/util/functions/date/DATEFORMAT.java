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
 * @date 2023/12/5
 */
public class DATEFORMAT extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        long source = FunctionUtils.getNumberValue(arg1, env).longValue();
        String format = FunctionUtils.getStringValue(arg2, env);
        SimpleDateFormat dateFormat = DateFormatCache.getOrCreateDateFormat(format);

        return AviatorRuntimeJavaType.valueOf(dateFormat.format(new Date(source)));
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.string;
    }



    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("DATEFORMAT函数可以将日期转换为指定格式")
                .usage("DATEFORMAT(时间戳)")
                .example("DATEFORMAT(1446307200000, 'yyyy-MM-dd') 返回 '2015-11-01'\n" +
                        "DATEFORMAT(1446307200000, 'yyyy-MM-dd HH:mm:ss') 返回 '2015-11-01 00:00:00'\n" +
                        "DATEFORMAT(NOW(), 'yyyy-MM-dd HH:mm:ss') 返回 '2023-12-01 00:00:00'");
    }
}
