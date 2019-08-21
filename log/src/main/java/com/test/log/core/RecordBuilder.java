package com.test.log.core;

/**
 * @author Shoven
 * @date 2019-07-26 16:22
 */
public interface RecordBuilder {

    Recordable build(RecordMeta recordMeta);
}
