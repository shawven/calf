package com.starter.log.core;

/**
 * @author Shoven
 * @date 2019-07-26 16:22
 */
public interface LogBuilder<T extends LogMeta> {

    Recordable build(T meta, JoinPointInfo joinPointInfo);
}
