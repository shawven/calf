package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        String namespace = context.getNodeConfig().getNamespace();
        DataPublisherManager dataPublisherManager = context.getDataPublisherManager();
        Map<String, Set<ClientInfo>> clientInfoMap = extractedClientInfoMap(context);

        this.updateEventHandler = new OpLogUpdateEventHandler(namespace, dataPublisherManager, clientInfoMap);
        this.writeEventHandler = new OpLogWriteEventHandler(namespace, dataPublisherManager, clientInfoMap);
        this.deleteEventHandler = new OpLogDeleteEventHandler(namespace, dataPublisherManager, clientInfoMap);
        this.defaultEventHandler = new OpLogDefaultEventHandler(namespace, dataPublisherManager, clientInfoMap);
    }

    private Map<String, Set<ClientInfo>> extractedClientInfoMap(OpLogEventContext context) {
        Map<String, Set<ClientInfo>> clientInfoMap = new HashMap<>();
        context.getClients().stream()
                .collect(Collectors.groupingBy(this::getClientInfoMapKey))
                .forEach((mapKey, clientInfoList) -> {
                    clientInfoMap.put(mapKey, new HashSet<>(clientInfoList));
                });
        return clientInfoMap;
    }

    private String getClientInfoMapKey(ClientInfo clientInfo) {
        return clientInfo.getDatabaseName().concat("/").concat(clientInfo.getTableName());
    }

    public AbstractOpLogEventHandler getHandler(String eventType) {
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
