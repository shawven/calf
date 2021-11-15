package com.github.shawven.calf.oplog;

import com.github.shawven.calf.extension.ClientDataSource;
import com.github.shawven.calf.extension.ConfigDataSource;
import com.github.shawven.calf.oplog.server.core.DistributorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author T-lih
 * @modified by
 */
@Component
public class BinLogApplicationRunner implements ApplicationRunner {

    @Autowired
    private Map<String, DistributorService> distributorServiceMap;

    @Autowired
    private ClientDataSource clientDataSource;

    @Autowired
    private ConfigDataSource configDataSource;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        distributorServiceMap.forEach((s, distributorService) -> {
            distributorService.startDistribute();
        });
//        configDataSource.registerConfigCommandWatcher();
    }
}
