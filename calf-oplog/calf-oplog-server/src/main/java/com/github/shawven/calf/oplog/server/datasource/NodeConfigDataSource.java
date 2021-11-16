package com.github.shawven.calf.extension;

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

    void registerWatcher() ;

    /**
     * 真正开启数据源的逻辑
     *
     * @param namespace
     * @param delegatedIp
     * @return
     */
    void start(String namespace, String delegatedIp);

    /**
     * 真正关闭数据源的逻辑
     *
     * @param namespace
     * @return
     */
    void stop(String namespace);


    List<String> getNamespaceList();

    void registerConfigCommandWatcher();
}
