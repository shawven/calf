package com.github.shawven.calf.util.functions.string;

import com.googlecode.aviator.runtime.function.string.StringStartsWithFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class STARTSWITH extends BaseFunction {

    private final StringStartsWithFunction delegate = new StringStartsWithFunction();

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
                .desc("STARTSWITH函数可以判断文本字符串是否以特定字符串开始，是则返回true，否则返回false")
                .usage("STARTSWITH('审批流程设置','审批')")
                .example("STARTSWITH('审批流程设置','审批')返回true");
    }
}

