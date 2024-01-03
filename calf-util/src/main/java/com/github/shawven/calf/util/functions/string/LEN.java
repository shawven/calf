package com.github.shawven.calf.util.functions.string;

import com.googlecode.aviator.runtime.function.string.StringLengthFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/9
 */
public class LEN extends BaseFunction {

    private final StringLengthFunction delegate = new StringLengthFunction();

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        return delegate.call(env, arg1);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.number;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("LEN函数可以获取文本中的字符个数")
                .usage("LEN(文本)")
                .example("LEN('今天天气不错')返回6，因为这句话中有6个字符");
    }
}

