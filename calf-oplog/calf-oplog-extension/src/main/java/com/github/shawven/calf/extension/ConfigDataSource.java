package com.github.shawven.calf.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author xw
 * @date 2021/11/15
 */
public interface ConfigDataSource {

    List<BinaryLogConfig> init(String dataSourceType);

    boolean create(BinaryLogConfig newConfig);

    void update(BinaryLogConfig newConfig);

    BinaryLogConfig remove(String namespace);

    BinaryLogConfig getByNamespace(String namespace);

    List<BinaryLogConfig> getAll();

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
