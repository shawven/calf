package com.starter.log;

import com.starter.log.core.*;
import org.aspectj.lang.JoinPoint;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-26 10:02
 */
public class RequestLogTask extends AbstractLogTask<RequestRecordMeta> {

    private RequestInfo requestInfo;

    public RequestLogTask(List<LogRepository> repositories,
                          RecordBuilder<RequestRecordMeta> recordBuilder,
                          JoinPoint joinPoint,
                          RequestInfo requestInfo) {
        super(repositories, recordBuilder, joinPoint);
        this.requestInfo = requestInfo;
    }

    @Override
    protected RequestRecordMeta makeRecordMeta(JoinPointInfo joinPointInfo) {
        RequestRecordMeta requestRecordMeta = new RequestRecordMeta(joinPointInfo, requestInfo);
        requestRecordMeta.setCause(getCause());
        requestRecordMeta.setCost(getCost());
        requestRecordMeta.setValue(getValue());
        return requestRecordMeta;
    }
}
