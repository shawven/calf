package com.starter.log;

import org.aspectj.lang.JoinPoint;

/**
 * @author Shoven
 * @date 2019-07-26 9:45
 */
public class RequestLogMeta extends DefaultLogMeta {

    private final RequestInfo requestInfo;

    public RequestLogMeta(JoinPoint joinPoint, RequestInfo requestInfo) {
        super(joinPoint);
        this.requestInfo = requestInfo;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }
}
