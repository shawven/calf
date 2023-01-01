package com.github.shawven.calf.oplog.server;

import com.github.shawven.calf.oplog.server.core.ReplicationServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;
import java.util.Map;

/**
 * @author T-lih
 * @modified by
 */
public class ServerRunner implements ApplicationRunner {

    private final List<ReplicationServer> serverList;

    public ServerRunner(List<ReplicationServer> serverList) {
        this.serverList = serverList;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        for (ReplicationServer server : serverList) {
            server.start();
        }
    }
}
