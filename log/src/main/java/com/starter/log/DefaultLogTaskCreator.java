package com.starter.log;

import com.starter.log.core.*;
import org.aspectj.lang.JoinPoint;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-27 17:51
 */
public class DefaultLogTaskCreator implements LogTaskCreator {

    private List<LogRepository> repositories;

    private RecordBuilder<DefaultRecordMeta> recordBuilder;

    public DefaultLogTaskCreator(List<LogRepository> repositories,
                                 RecordBuilder<DefaultRecordMeta> recordBuilder) {
        this.repositories = repositories;
        this.recordBuilder = recordBuilder;
    }

    @Override
    public LogTask create(JoinPoint jp, Object value, long cost) {
        DefaultLogTask logTask = new DefaultLogTask(getRepositories(), getRecordBuilder(), jp);
        logTask.setValue(value);
        logTask.setCost(cost);
        return logTask;
    }

    @Override
    public LogTask create(JoinPoint jp, Throwable cause, long cost) {
        DefaultLogTask logTask = new DefaultLogTask(getRepositories(), getRecordBuilder(), jp);
        logTask.setCause(cause);
        logTask.setCost(cost);
        return logTask;
    }

    public List<LogRepository> getRepositories() {
        return repositories;
    }

    public RecordBuilder<DefaultRecordMeta> getRecordBuilder() {
        return recordBuilder;
    }
}
