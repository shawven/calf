package com.github.shawven.calf.log;

import com.github.shawven.calf.log.core.LogBuilder;
import com.github.shawven.calf.log.core.Recordable;
import com.github.shawven.calf.log.core.JoinPointInfo;

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
