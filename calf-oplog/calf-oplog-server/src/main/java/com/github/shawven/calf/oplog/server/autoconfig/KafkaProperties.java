package com.github.shawven.calf.oplog.server.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * @author: kl @kailing.pub
 * @date: 2019/5/24
 */
@ConditionalOnProperty("spring.kafka.bootstrap-servers")
public class KafkaProperties {

    private String servers = "192.168.1.81:2181";

    private int connectionTimeout = 30000;

    private int sessionTimeout = 30000;
    /**
     * kafka默认分区数
     */
    private int partitions = 1;
    /**
     * kafka默认副本数
     */
    private int replications = 1;


    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public int getReplications() {
        return replications;
    }

    public void setReplications(int replications) {
        this.replications = replications;
    }
}
