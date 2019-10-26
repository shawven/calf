package com.starter.log;

import com.starter.log.core.*;
import org.aspectj.lang.JoinPoint;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-27 17:51
 */
public class DefaultLogTaskCreator implements LogTaskCreator {

    private JoinPointExtractor extractor;

    private List<LogRepository> repositories;

    private RecordBuilder recordBuilder;

    public DefaultLogTaskCreator(JoinPointExtractor extractor,
                                 List<LogRepository> repositories,
                                 RecordBuilder recordBuilder) {
        this.extractor = extractor;
        this.repositories = repositories;
        this.recordBuilder = recordBuilder;
    }

    @Override
    public LogTask create(JoinPoint jp, Object value, Throwable cause, long cost) {
        DefaultLogTask logTask = new DefaultLogTask(extractor, repositories, recordBuilder, jp);
        logTask.setCause(cause);
        logTask.setCost(cost);
        return logTask;
    }

    public JoinPointExtractor getExtractor() {
        return extractor;
    }

    public void setExtractor(JoinPointExtractor extractor) {
        this.extractor = extractor;
    }

    public List<LogRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<LogRepository> repositories) {
        this.repositories = repositories;
    }

    public RecordBuilder getRecordBuilder() {
        return recordBuilder;
    }

    public void setRecordBuilder(RecordBuilder recordBuilder) {
        this.recordBuilder = recordBuilder;
    }

}
