package com.github.shawven.calf.util.functions.high;

import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.FunctionDoc;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class CURRENTDEPT extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env) {
        return new AviatorString("");
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.string;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("CURRENTDEPT函数可以获取当前登陆人部门")
                .usage("CURRENTDEPT()")
                .example("CURRENTDEPT()");
    }
}
