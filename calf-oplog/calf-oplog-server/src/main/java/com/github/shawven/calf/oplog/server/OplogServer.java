package com.github.shawven.calf.oplog.server;

import com.github.shawven.calf.oplog.server.core.ReplicationServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Map;

/**
 * @author T-lih
 * @modified by
 */
public class OplogServer implements ApplicationRunner {

    private final Map<String, ReplicationServer> distributorServiceMap;

    public OplogServer(Map<String, ReplicationServer> distributorServiceMap) {
        this.distributorServiceMap = distributorServiceMap;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        distributorServiceMap.forEach((s, replicationServer) -> {
            replicationServer.start();
        });
    }
}
