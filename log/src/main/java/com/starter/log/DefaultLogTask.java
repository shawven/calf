package com.starter.log;

import com.starter.log.core.*;
import org.aspectj.lang.JoinPoint;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-27 17:52
 */
public class DefaultLogTask extends AbstractLogTask<DefaultRecordMeta> {

    public DefaultLogTask(List<LogRepository> repositories,
                          RecordBuilder<DefaultRecordMeta> recordBuilder,
                          JoinPoint joinPoint) {
        super(repositories, recordBuilder, joinPoint);
    }

    @Override
    protected DefaultRecordMeta makeRecordMeta(JoinPointInfo joinPointInfo) {
        DefaultRecordMeta defaultRecordMeta = new DefaultRecordMeta(joinPointInfo);
        defaultRecordMeta.setCause(getCause());
        defaultRecordMeta.setValue(getValue());
        defaultRecordMeta.setCost(getCost());
        return defaultRecordMeta;
    }
}
