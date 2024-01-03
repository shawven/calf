package com.github.shawven.calf.util.functions.string;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class RIGHT extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String target = FunctionUtils.getStringValue(arg1, env);
        int len = FunctionUtils.getNumberValue(arg2, env).intValue();

        return new AviatorString(StringUtils.right(target, len));
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.string;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("RIGHT")
                .usage("RIGHT(文本,数字)")
                .example("RIGHT('0001',3)，返回'001'");
    }
}
