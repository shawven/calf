package com.github.shawven.calf.util.functions.string;

import com.googlecode.aviator.runtime.function.string.StringSplitFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class SPLIT extends BaseFunction {

    private final StringSplitFunction delegate = new StringSplitFunction();

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        return delegate.call(env, arg1, arg2);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.array;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("SPLIT函教可以将文本按指定分割符分割成数组")
                .usage("SPLIT(文本,分隔符_文本)")
                .example("SPLIT('轻云-表单','-')返回'轻云'，'表单'");
    }
}
