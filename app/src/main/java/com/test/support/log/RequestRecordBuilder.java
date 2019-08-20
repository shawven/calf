package com.test.support.log;

import com.test.support.log.core.JoinPointInfo;
import com.test.support.log.core.RecordMeta;
import com.test.support.log.core.Recordable;
import com.test.support.log.emun.LogType;

import java.util.Date;

/**
 * @author Shoven
 * @date 2019-07-26 15:16
 */
public class RequestRecordBuilder extends DefaultRecordBuilder {

    @Override
    public Recordable build(RecordMeta recordMeta) {
        RequestRecordMeta meta = (RequestRecordMeta) recordMeta;
        RequestInfo requestInfo = meta.getRequestInfo();
        JoinPointInfo joinPointInfo = meta.getJoinPointInfo();

        RequestRecord record = new RequestRecord();
        record.setType(LogType.valueOf(record.getTypeName()).getType());
        record.setIp(requestInfo.getIp());
        record.setAddress(requestInfo.getIpAddress());

        record.setRequestUrl(requestInfo.getPath());
        record.setRequestMethod(requestInfo.getMethod());
        record.setRequestParams(String.valueOf(requestInfo.getParameters()));
        record.setRequestHeaders(String.valueOf(requestInfo.getHeaders()));
        record.setMethod(joinPointInfo.getMethod().getName());

        record.setCreateTime(new Date());
        record.setCost(meta.getCost());
        record.setError(meta.getCause().getMessage());
        return record;
    }
}

