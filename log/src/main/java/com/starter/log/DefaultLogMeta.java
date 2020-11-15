package com.starter.log;

import com.starter.log.core.LogMeta;
import org.aspectj.lang.JoinPoint;

/**
 * @author Shoven
 * @date 2019-07-26 9:47
 */
public class DefaultLogMeta implements LogMeta {

    private final JoinPoint joinPoint;

    private Throwable cause;

    private Object value;

    private long cost;

    public DefaultLogMeta(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    @Override
    public JoinPoint getJoinPoint() {
        return joinPoint;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long usage) {
        this.cost = usage;
    }
}
