package com.starter.log;

import com.starter.log.core.*;
import org.aspectj.lang.JoinPoint;

/**
 * @author Shoven
 * @date 2019-07-27 17:51
 */
public class DefaultLogMetaCreator implements LogMetaCreator<DefaultLogMeta> {

    @Override
    public DefaultLogMeta create(JoinPoint jp, Object value, long cost) {
        DefaultLogMeta defaultLogMeta = new DefaultLogMeta(jp);
        defaultLogMeta.setValue(value);
        defaultLogMeta.setCost(cost);
        return defaultLogMeta;
    }

    @Override
    public DefaultLogMeta create(JoinPoint jp, Throwable cause, long cost) {
        DefaultLogMeta defaultLogMeta = new DefaultLogMeta(jp);
        defaultLogMeta.setCause(cause);
        defaultLogMeta.setCost(cost);
        return defaultLogMeta;
    }
}
