package com.starter.oplog.client;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/4:26 PM
 * @modified by
 */
public interface DataSubscriber {
    void subscribe(String clientId, BinLogDistributorClient binLogDistributorClient);
}
