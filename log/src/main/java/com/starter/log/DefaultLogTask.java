package com.starter.log;

import com.starter.log.core.*;
import org.aspectj.lang.JoinPoint;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-27 17:52
 */
public class DefaultLogTask extends AbstractLogTask {

    private Throwable cause;

    private long cost;

    public DefaultLogTask(JoinPointExtractor joinPointExtractor,
                          List<LogRepository> repositories,
                          RecordBuilder recordBuilder,
                          JoinPoint joinPoint) {
        super(joinPointExtractor, repositories, recordBuilder, joinPoint);
    }

    @Override
    protected RecordMeta makeRecordMeta(JoinPointInfo joinPointInfo) {
        DefaultRecordMeta defaultRecordMeta = new DefaultRecordMeta(joinPointInfo);
        defaultRecordMeta.setCause(cause);
        defaultRecordMeta.setCost(cost);
        return defaultRecordMeta;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }
}
