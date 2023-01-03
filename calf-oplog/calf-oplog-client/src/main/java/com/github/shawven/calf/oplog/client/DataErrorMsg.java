package com.github.shawven.calf.oplog.client;

import com.github.shawven.calf.oplog.base.EventBaseDTO;

import java.io.Serializable;

/**
 * @author zhenhui
 * @Ddate Created in 2018/27/01/2018/3:24 PM
 * @modified by
 */
public class DataErrorMsg implements Serializable {

    private static final long serialVersionUID = 4556335110286780329L;

    private EventBaseDTO eventBaseDTO;
    private Exception exception;
    private String dataKey;

    public DataErrorMsg() {
    }

    public DataErrorMsg(EventBaseDTO eventBaseDTO, Exception exception) {
        this.eventBaseDTO = eventBaseDTO;
        this.exception = exception;
    }

    public DataErrorMsg(EventBaseDTO eventBaseDTO, Exception exception, String dataKey) {
        this.eventBaseDTO = eventBaseDTO;
        this.exception = exception;
        this.dataKey = dataKey;
    }

    public EventBaseDTO getEventBaseDTO() {
        return eventBaseDTO;
    }

    public void setEventBaseDTO(EventBaseDTO eventBaseDTO) {
        this.eventBaseDTO = eventBaseDTO;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }
}
