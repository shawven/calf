package com.starter.log;

import com.starter.log.core.RecordBuilder;
import com.starter.log.core.RecordMeta;
import com.starter.log.core.Recordable;

/**
 * @author Shoven
 * @date 2019-07-26 16:30
 */
public class DefaultRecordBuilder implements RecordBuilder<DefaultRecordMeta> {

    @Override
    public Recordable build(DefaultRecordMeta meta) {
        DefaultRecord record = new DefaultRecord();
        record.setCost(meta.getCost());
        record.setError(meta.getCause().getMessage());
        return record;
    }
}
