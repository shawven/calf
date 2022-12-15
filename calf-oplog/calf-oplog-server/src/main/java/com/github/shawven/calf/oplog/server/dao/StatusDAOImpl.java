package com.github.shawven.calf.oplog.server.dao;

import com.alibaba.fastjson.JSON;
import com.github.shawven.calf.oplog.base.Consts;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.register.domain.DataSourceStatus;
import com.github.shawven.calf.oplog.register.domain.InstanceStatus;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.register.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatusDAOImpl implements StatusDAO {

    private final Repository repository;

    private final KeyPrefixUtil keyPrefixUtil;

    private final DataSourceCfgDAO dataSourceCfgDAO;

    public StatusDAOImpl(Repository repository, KeyPrefixUtil keyPrefixUtil, DataSourceCfgDAO dataSourceCfgDAO) {
        this.repository = repository;
        this.keyPrefixUtil = keyPrefixUtil;
        this.dataSourceCfgDAO = dataSourceCfgDAO;
    }

    @Override
    public void updateDataSourceStatus(String filename, long position, DataSourceCfg config) {
        long now = System.currentTimeMillis();
        String dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_DATE_TIME);

        String namespace = config.getNamespace();
        String statusKey = config.getStatusKey();

        String metaData = repository.get(getKey(namespace, statusKey));
        DataSourceStatus status;
        if(!StringUtils.hasText(metaData)) {
            status = new DataSourceStatus();
            status.setNamespace(config.getNamespace());
        } else {
            status = JSON.parseObject(metaData).toJavaObject(DataSourceStatus.class);
        }

        status.setFilename(filename);
        status.setPosition(position);
        status.setTimestamp(now);
        status.setDateTime(dateTime);

        repository.set(getKey(namespace, statusKey), JSON.toJSONString(status));
    }

    @Override
    public List<DataSourceStatus> listStatus() {
        return dataSourceCfgDAO.getAll().stream()
                .map(config -> {
                    String metaData = repository.get(getKey(config.getNamespace(), config.getStatusKey()));
                    if (metaData == null) {
                        return null;
                    }
                    // metaData shouldn't be null
                    return JSON.parseObject(metaData).toJavaObject(DataSourceStatus.class);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public DataSourceStatus getDataSourceStatus(DataSourceCfg dataSourceCfg) {
        String namespace = dataSourceCfg.getNamespace();
        String statusKey = dataSourceCfg.getStatusKey();

        String metaData = repository.get(getKey(namespace, statusKey));
        if(!StringUtils.hasText(metaData)) {
            return null;
        }
        return JSON.parseObject(metaData, DataSourceStatus.class);
    }


    @Override
    public void updateInstanceStatus(String serviceKey, InstanceStatus status) {
        repository.set(
                keyPrefixUtil.withPrefix(Consts.SERVICE_STATUS_PATH).concat(serviceKey),
                JSON.toJSONString(status),
                20
        );
    }

    /**
     * @return
     */
    @Override
    public List<InstanceStatus> getInstanceStatus() {
        List<String> strings = repository.list(keyPrefixUtil.withPrefix(Consts.SERVICE_STATUS_PATH));
        return strings.stream()
                .map(str -> JSON.parseObject(str, InstanceStatus.class))
                .collect(Collectors.toList());
    }

    private String getKey(String namespace, String key) {
        return keyPrefixUtil.withPrefix(namespace) + "/" + key;
    }
}
