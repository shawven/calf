package com.starter.log;

import com.starter.log.core.JoinPointInfo;
import com.starter.log.core.LogBuilder;
import com.starter.log.core.Recordable;
import com.starter.log.emun.LogType;

import java.util.Date;

/**
 * @author Shoven
 * @date 2019-07-26 15:16
 */
public class RequestLogBuilder implements LogBuilder<RequestLogMeta> {

    @Override
    public Recordable build(RequestLogMeta meta, JoinPointInfo joinPointInfo) {
        RequestInfo requestInfo = meta.getRequestInfo();

        RequestLog log = new RequestLog();
        log.setIp(requestInfo.getIp());
        log.setAddress("");

        log.setRequestUrl(requestInfo.getPath());
        log.setRequestMethod(requestInfo.getMethod());
        log.setRequestParams(String.valueOf(requestInfo.getParameters()));
        log.setRequestHeaders(String.valueOf(requestInfo.getHeaders()));
        log.setMethod(joinPointInfo.getMethod().getName());

        log.setCreateTime(new Date());
        log.setCost(meta.getCost());
        log.setError(meta.getCause().getMessage());
        return log;
    }
}

