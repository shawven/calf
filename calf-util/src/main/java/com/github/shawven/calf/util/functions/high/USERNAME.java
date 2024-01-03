package com.github.shawven.calf.util.functions.high;

import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.FunctionDoc;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Collection;
import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class USERNAME extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String oid;
        Object object = FunctionUtils.getJavaObject(arg1, env);
        if (object instanceof Collection) {
            oid = String.valueOf(((Collection)object).iterator().next());
        } else {
            oid = String.valueOf(object);
        }
        return AviatorNil.NIL;
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.string;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("USERDEPT函数用于获取指定人姓名")
                .usage("USERDEPT(oid)")
                .example("USERDEPT(人员控件)");
    }
}

