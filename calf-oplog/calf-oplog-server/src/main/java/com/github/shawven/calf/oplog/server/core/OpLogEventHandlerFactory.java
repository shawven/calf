package com.github.shawven.calf.oplog.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogEventHandlerFactory {
    private static final Logger log = LoggerFactory.getLogger(OpLogEventHandlerFactory.class);

    private final OpLogUpdateEventHandler updateEventHandler;

    private final OpLogWriteEventHandler writeEventHandler;

    private final OpLogDeleteEventHandler deleteEventHandler;

    private final OpLogDefaultEventHandler defaultEventHandler;

    public OpLogEventHandlerFactory(OpLogEventContext context) {

        this.updateEventHandler = new OpLogUpdateEventHandler(context);
        this.writeEventHandler = new OpLogWriteEventHandler(context);
        this.deleteEventHandler = new OpLogDeleteEventHandler(context);
        this.defaultEventHandler = new OpLogDefaultEventHandler(context);
    }

    public OpLogEventHandler getHandler(String eventType) {
        switch (eventType) {
            case "u":
                return updateEventHandler;
            case "i":
                return writeEventHandler;
            case "d":
                return deleteEventHandler;
            default:
                return defaultEventHandler;
        }
    }
}
