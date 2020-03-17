package com.starter.log;

import com.starter.log.core.*;
import org.aspectj.lang.JoinPoint;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-26 11:13
 */
public class RequestLogTaskCreator implements LogTaskCreator {

    private List<LogRepository> repositories;

    private RecordBuilder<RequestRecordMeta> recordBuilder;

    public RequestLogTaskCreator(List<LogRepository> repositories,
                                 RecordBuilder<RequestRecordMeta> recordBuilder) {
        this.repositories = repositories;
        this.recordBuilder = recordBuilder;
    }

    @Override
    public LogTask create(JoinPoint jp, Object value, long cost) {
        HttpServletRequest request = UserContext.getRequest();
        RequestInfo requestInfo = new RequestInfo(request);
        RequestLogTask requestLogTask = new RequestLogTask(getRepositories(), getRecordBuilder(),
                jp, requestInfo);
        requestLogTask.setValue(value);
        requestLogTask.setCost(cost);
        return requestLogTask;
    }

    @Override
    public LogTask create(JoinPoint jp, Throwable cause, long cost) {
        HttpServletRequest request = UserContext.getRequest();
        RequestInfo requestInfo = new RequestInfo(request);
        RequestLogTask requestLogTask = new RequestLogTask(getRepositories(), getRecordBuilder(),
                jp, requestInfo);
        requestLogTask.setCause(cause);
        requestLogTask.setCost(cost);
        return requestLogTask;
    }

    public List<LogRepository> getRepositories() {
        return repositories;
    }

    public RecordBuilder<RequestRecordMeta> getRecordBuilder() {
        return recordBuilder;
    }
}
