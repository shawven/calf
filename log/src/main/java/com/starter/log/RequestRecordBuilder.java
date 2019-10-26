package com.starter.log;

import com.starter.log.core.JoinPointInfo;
import com.starter.log.core.RecordMeta;
import com.starter.log.core.Recordable;
import com.starter.log.emun.LogType;

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
        record.setAddress("");

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

