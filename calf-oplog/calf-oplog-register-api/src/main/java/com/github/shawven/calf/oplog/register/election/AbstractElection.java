package com.github.shawven.calf.oplog.register.election;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author xw
 * @date 2022/12/9
 */
public abstract class AbstractElection implements Election {

    private final Logger logger = LoggerFactory.getLogger(AbstractElection.class);

    /**
     * keep racing for leadership when losing
     */
    private final boolean autoRequeue;

    /**
     * invoke when acquire leadership
     */
    private final ElectionListener electionListener;

    public AbstractElection(boolean autoRequeue, ) {
        this.autoRequeue = autoRequeue;
    }

    /**
     * start racing for leadership.
     */
    @Override
    public void start() {

    }
}
