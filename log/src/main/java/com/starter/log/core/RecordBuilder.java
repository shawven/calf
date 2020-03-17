package com.starter.log.core;

/**
 * @author Shoven
 * @date 2019-07-26 16:22
 */
public interface RecordBuilder<T extends RecordMeta> {

    Recordable build(T meta);
}
