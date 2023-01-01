package com.github.shawven.calf.oplog.server.ops;


import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.register.domain.DataSourceStatus;
import com.github.shawven.calf.oplog.register.domain.InstanceStatus;

import java.util.List;

public interface StatusOps {

    List<DataSourceStatus> listStatus();

    void updateDataSourceStatus(String filename, long position, DataSourceCfg dataSourceCfg);

    DataSourceStatus getDataSourceStatus(DataSourceCfg dataSourceCfg);

    void updateInstanceStatus(String serviceKey, InstanceStatus status);

    List<InstanceStatus> getInstanceStatus();
}
