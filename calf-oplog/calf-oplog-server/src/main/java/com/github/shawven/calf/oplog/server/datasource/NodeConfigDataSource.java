package com.github.shawven.calf.oplog.server.datasource;

import com.github.shawven.calf.oplog.server.mode.Command;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author xw
 * @date 2021/11/15
 */
public interface NodeConfigDataSource {

    List<NodeConfig> init(String dataSourceType);

    boolean create(NodeConfig newConfig);

    void update(NodeConfig newConfig);

    NodeConfig remove(String namespace);

    NodeConfig getByNamespace(String namespace);

    List<NodeConfig> getAll();

    CompletableFuture<List<NodeConfig>> asyncGetAll();

    List<String> getNamespaceList();

    void registerServiceWatcher(ServiceWatcher watcher);

    interface ServiceWatcher {

        void start(Command command);

        void stop(Command command);
    }

}
