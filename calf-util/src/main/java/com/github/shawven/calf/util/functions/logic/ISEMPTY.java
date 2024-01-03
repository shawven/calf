package com.github.shawven.calf.util.functions.logic;

import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.utils.ArrayUtils;
import com.googlecode.aviator.utils.TypeUtils;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class ISEMPTY extends BaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object value = arg1.getValue(env);
        if (value == null) {
            return AviatorBoolean.TRUE;
        } else if (TypeUtils.isString(value)) {
            return AviatorBoolean.valueOf(StringUtils.isEmpty(String.valueOf(value)));
        } else if (value.getClass().isArray()) {
            return AviatorBoolean.valueOf(ArrayUtils.getLength(value) == 0);
        }
        return AviatorBoolean.FALSE;
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("函数可以用来判断值是否为空文本、空对象或者空数组")
                .usage("ISEMPTY(文本)")
                .example("ISEMPTY(文本)");
    }
}
