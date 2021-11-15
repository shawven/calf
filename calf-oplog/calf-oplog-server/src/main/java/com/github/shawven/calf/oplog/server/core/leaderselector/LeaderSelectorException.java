package com.github.shawven.calf.oplog.server.core.leaderselector;

/**
 * @author wanglaomo
 * @since 2019/7/29
 **/
public class LeaderSelectorException extends Exception {

    public LeaderSelectorException(String message) {
        super(message);
    }

    public LeaderSelectorException(String message, Exception cause) {
        super(message, cause);
    }
}
