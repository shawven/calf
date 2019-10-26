package com.starter.log;

import com.starter.log.core.JoinPointInfo;
import com.starter.log.core.RecordMeta;

/**
 * @author Shoven
 * @date 2019-07-26 9:47
 */
public class DefaultRecordMeta implements RecordMeta {

    private JoinPointInfo joinPointInfo;

    private Throwable cause;

    private long cost;

    public DefaultRecordMeta(JoinPointInfo joinPointInfo) {
        this.joinPointInfo = joinPointInfo;
    }

    @Override
    public JoinPointInfo getJoinPointInfo() {
        return joinPointInfo;
    }

    public void setJoinPointInfo(JoinPointInfo joinPointInfo) {
        this.joinPointInfo = joinPointInfo;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long usage) {
        this.cost = usage;
    }
}
