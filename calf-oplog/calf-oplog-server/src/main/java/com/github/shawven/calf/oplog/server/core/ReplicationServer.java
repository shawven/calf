package com.github.shawven.calf.oplog.server.core;



import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.register.domain.InstanceStatus;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/10
 **/
public interface ReplicationServer {

    /**
     * 数据源类型
     *
     * @return
     */
    String dataSourceType();

    void start();

    void stop(String namespace);


}
