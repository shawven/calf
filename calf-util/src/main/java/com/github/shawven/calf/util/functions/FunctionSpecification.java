package com.github.shawven.calf.util.functions;

import com.github.shawven.calf.util.functions.FormulaTypeEnum;

/**
 * @author xw
 * @date 2023/12/9
 */
public interface FunctionSpecification {

    /**
     * 函数名
     *
     * @return
     */
    String getName();

    /**
     * 输出类型
     *
     * @return
     */
    default FormulaTypeEnum output() {return null;};

    /**
     * 文档
     *
     * @return
     */
    default FunctionDoc doc() {return null;};
}
