package com.starter.log;

import com.starter.log.core.*;
import org.aspectj.lang.JoinPoint;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Shoven
 * @date 2019-07-26 11:13
 */
public class RequestLogMetaCreator implements LogMetaCreator<RequestLogMeta> {

    @Override
    public RequestLogMeta create(JoinPoint jp, Object value, long cost) {
        HttpServletRequest request = getRequest();
        RequestInfo requestInfo = new RequestInfo(request);
        RequestLogMeta requestRecordMeta = new RequestLogMeta(jp, requestInfo);
        requestRecordMeta.setValue(value);
        requestRecordMeta.setCost(cost);
        return requestRecordMeta;
    }

    @Override
    public RequestLogMeta create(JoinPoint jp, Throwable cause, long cost) {
        HttpServletRequest request = getRequest();
        RequestInfo requestInfo = new RequestInfo(request);
        RequestLogMeta requestRecordMeta = new RequestLogMeta(jp, requestInfo);
        requestRecordMeta.setCause(cause);
        requestRecordMeta.setCost(cost);
        return requestRecordMeta;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
