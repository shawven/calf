package com.github.shawven.calf.oplog.server.core;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogEventFormatterFactory {

    private final OpLogEventFormatter updateEventFormatter;

    private final OpLogEventFormatter writeEventFormatter;

    private final OpLogEventFormatter deleteEventFormatter;

    private final OpLogEventFormatter defaultEventFormatter;

    public OpLogEventFormatterFactory(String namespace) {
        this.writeEventFormatter = new OpLogEventFormatter.Write(namespace);
        this.updateEventFormatter = new OpLogEventFormatter.Update(namespace);
        this.deleteEventFormatter = new OpLogEventFormatter.Delete(namespace);

        this.defaultEventFormatter = event -> null;
    }

    public OpLogEventFormatter getFormatter(String eventType) {
        switch (eventType) {
            case "u":
                return updateEventFormatter;
            case "i":
                return writeEventFormatter;
            case "d":
                return deleteEventFormatter;
            default:
                return defaultEventFormatter;
        }
    }
}
