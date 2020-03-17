package com.starter.log;

import com.starter.log.core.JoinPointInfo;
import com.starter.log.core.LogBuilder;
import com.starter.log.core.Recordable;

/**
 * @author Shoven
 * @date 2019-07-26 16:30
 */
public class DefaultLogBuilder implements LogBuilder<DefaultLogMeta> {

    @Override
    public Recordable build(DefaultLogMeta meta, JoinPointInfo joinPointInfo) {
        DefaultLog log = new DefaultLog();
        log.setCost(meta.getCost());
        log.setError(meta.getCause().getMessage());
        return log;
    }
}
