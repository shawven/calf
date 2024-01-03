package com.github.shawven.calf.util.functions.high;

import com.googlecode.aviator.runtime.function.seq.SeqMapFunction;
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
public class MAP extends BaseFunction implements LightLambda {

    private final SeqMapFunction delegate = new SeqMapFunction();

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
                .desc("MAP函数用于映射一个数组，将谓词作用在数组的每个元素上, 返回新元素组成的集合")
                .usage("MAP(数组，谓词)")
                .example("MAP(V, __elem__ + 10), V是一个明细内的成绩控件, V含有[60,70,80]，则返回数组[70,80,90]; 实现所有成绩+10分的效果；" +
                        "MAP(V, USERDEPT(__elem__)), V是一个明细内的人员控件, V含有[张三, 李四, 王五]，张三属于开发部, 李四属于人事部, 王五属于财务部，则返回数组['开发部','人事部','财务部'];");
    }
}
