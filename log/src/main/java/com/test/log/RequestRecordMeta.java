package com.test.log;

import com.test.log.core.JoinPointInfo;

/**
 * @author Shoven
 * @date 2019-07-26 9:45
 */
public class RequestRecordMeta extends DefaultRecordMeta {

    private RequestInfo requestInfo;

    private Object value;

    public RequestRecordMeta(JoinPointInfo joinPointInfo, RequestInfo requestInfo) {
        super(joinPointInfo);
        this.requestInfo = requestInfo;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
