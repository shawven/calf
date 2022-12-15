package com.github.shawven.calf.oplog.server.dao;

import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.server.ServerWatcher;

import java.util.List;

/**
 * @author xw
 * @date 2021/11/15
 */
public interface DataSourceCfgDAO {

    List<DataSourceCfg> init(String type);

    boolean create(DataSourceCfg newConfig);

    void update(DataSourceCfg newConfig);

    boolean remove(String namespace);

    DataSourceCfg getByNamespace(String namespace);

    List<DataSourceCfg> getAll();

    List<String> getNamespaceList();

    void registerServerWatcher(ServerWatcher watcher);
}
