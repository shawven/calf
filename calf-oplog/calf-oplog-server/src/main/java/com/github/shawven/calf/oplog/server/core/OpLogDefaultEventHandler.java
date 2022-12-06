package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogDefaultEventHandler extends AbstractOpLogEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OpLogDefaultEventHandler.class);

    public OpLogDefaultEventHandler(String namespace, DataPublisherManager dataPublisherManager, Map<String, Set<ClientInfo>> clientInfoMap) {
        super(namespace, dataPublisherManager, clientInfoMap);
    }

    @Override
    public void handle(Document event) {
        log.info("跳过不处理的MongoDB事件event:{}", event);
    }

}
