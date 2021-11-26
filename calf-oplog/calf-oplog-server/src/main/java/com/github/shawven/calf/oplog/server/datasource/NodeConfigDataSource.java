package com.github.shawven.calf.oplog.server.datasource;

import com.github.shawven.calf.oplog.server.core.ServiceSwitcher;

import java.util.*;

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

    List<String> getNamespaceList();

    void registerWatcher(ServiceSwitcher serviceSwitcher);

}
