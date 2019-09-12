package com.test.log;

import com.test.log.core.*;
import org.aspectj.lang.JoinPoint;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-26 10:02
 */
public class RequestLogTask extends DefaultLogTask {

    private RequestInfo requestInfo;

    private Object value;

    public RequestLogTask(JoinPointExtractor joinPointExtractor,
                          List<LogRepository> repositories,
                          RecordBuilder recordBuilder,
                          JoinPoint joinPoint,
                          RequestInfo requestInfo) {
        super(joinPointExtractor, repositories, recordBuilder, joinPoint);
        this.requestInfo = requestInfo;
    }

    @Override
    protected RecordMeta makeRecordMeta(JoinPointInfo joinPointInfo) {
        RequestRecordMeta requestRecordMeta = new RequestRecordMeta(joinPointInfo, requestInfo);
        requestRecordMeta.setCause(getCause());
        requestRecordMeta.setCost(getCost());
        requestRecordMeta.setValue(value);
        return requestRecordMeta;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
