package com.github.shawven.calf.util.functions.high;

import com.googlecode.aviator.runtime.function.seq.SeqFilterFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.github.shawven.calf.util.functions.FormulaTypeEnum;
import com.github.shawven.calf.util.functions.BaseFunction;
import com.github.shawven.calf.util.functions.FunctionDoc;
import com.github.shawven.calf.util.functions.LightLambda;

import java.util.Map;

/**
 * @author xw
 * @date 2023/12/5
 */
public class FILTER extends BaseFunction implements LightLambda {

    private final SeqFilterFunction delegate = new SeqFilterFunction();

    @Override
    public AviatorObject call(final Map<String, Object> env, final AviatorObject arg1, final AviatorObject arg2) {
        AviatorObject function = buildFunction(env, arg2, 1);

        return delegate.call(env, arg1, function);
    }

    @Override
    public FormulaTypeEnum output() {
        return FormulaTypeEnum.array;
    }

    @Override
    public FunctionDoc doc() {
        return FunctionDoc.builder()
                .desc("FILTER函数用于过滤一个数组，将谓词作用在数组的每个元素上, 返回谓词为true的元素组成的数组")
                .usage("FILTER(数组，谓词)")
                .example("FILTER(V, __elem__ >= 2), V是一个明细内的数字控件, V含有[1,2,3]，则返回数组[2,3]，返回大于等于2的元素；" +
                        "FILTER(V, STARTSWITH(__elem__, '李'), V是一个明细内的姓名控件, V含有['张三','李四','王五']，则返回数组['李四']，返回大于姓氏为李的元素；");
    }
}
