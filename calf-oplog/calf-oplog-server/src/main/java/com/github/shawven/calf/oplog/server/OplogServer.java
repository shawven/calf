package com.github.shawven.calf.oplog.server;

import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.core.DistributorService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Map;

/**
 * @author T-lih
 * @modified by
 */
public class OplogServer implements ApplicationRunner {

    private Map<String, DistributorService> distributorServiceMap;

    private ClientDataSource clientDataSource;

    private NodeConfigDataSource nodeConfigDataSource;

    public OplogServer(Map<String, DistributorService> distributorServiceMap,
                       ClientDataSource clientDataSource,
                       NodeConfigDataSource nodeConfigDataSource) {
        this.distributorServiceMap = distributorServiceMap;
        this.clientDataSource = clientDataSource;
        this.nodeConfigDataSource = nodeConfigDataSource;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        distributorServiceMap.forEach((s, distributorService) -> {
            distributorService.startDistribute();
        });
    }
}
