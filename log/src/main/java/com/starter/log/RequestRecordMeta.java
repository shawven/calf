package com.starter.log;

import com.starter.log.core.JoinPointInfo;

/**
 * @author Shoven
 * @date 2019-07-26 9:45
 */
public class RequestRecordMeta extends DefaultRecordMeta {

    private RequestInfo requestInfo;


    public RequestRecordMeta(JoinPointInfo joinPointInfo, RequestInfo requestInfo) {
        super(joinPointInfo);
        this.requestInfo = requestInfo;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }
}
