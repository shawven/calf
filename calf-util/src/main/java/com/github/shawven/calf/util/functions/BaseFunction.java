package com.github.shawven.calf.util.functions;

import com.googlecode.aviator.runtime.function.AbstractFunction;

/**
 * @author xw
 * @date 2023/12/5
 */
public abstract class BaseFunction extends AbstractFunction implements FunctionSpecification {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
