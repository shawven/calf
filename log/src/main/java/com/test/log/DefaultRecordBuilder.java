package com.test.log;

import com.test.log.core.RecordBuilder;
import com.test.log.core.RecordMeta;
import com.test.log.core.Recordable;

/**
 * @author Shoven
 * @date 2019-07-26 16:30
 */
public class DefaultRecordBuilder implements RecordBuilder {

    @Override
    public Recordable build(RecordMeta recordMeta) {
        DefaultRecordMeta meta = (DefaultRecordMeta) recordMeta;
        DefaultRecord record = new DefaultRecord();
        record.setCost(meta.getCost());
        record.setError(meta.getCause().getMessage());
        return record;
    }
}
