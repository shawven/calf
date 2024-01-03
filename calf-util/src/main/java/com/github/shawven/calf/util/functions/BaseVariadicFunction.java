package com.github.shawven.calf.util.functions;

import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;

/**
 * @author xw
 * @date 2023/12/5
 */
public abstract class BaseVariadicFunction extends AbstractVariadicFunction implements FunctionSpecification {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
