package com.github.shawven.calf.util.functions.string;

import com.googlecode.aviator.runtime.function.string.StringEndsWithFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class ENDSWITH extends BaseFunction {

    private final StringEndsWithFunction delegate = new StringEndsWithFunction();

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        return delegate.call(env, arg1, arg2);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.bool;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("STARTSWITH函数可以判断文本字符串是否以特定字符串结尾，是则返回true，否则返回false")
                .usage("STARTSWITH('审批流程设置','流程设置')")
                .example("STARTSWITH('审批流程设置','流程设置')返回true");
    }
}
