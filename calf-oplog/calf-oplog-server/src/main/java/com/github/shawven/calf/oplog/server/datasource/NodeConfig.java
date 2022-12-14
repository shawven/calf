package com.github.shawven.calf.oplog.server.datasource;

/**
 * @author T-lih
 */
public class NodeConfig {

    private String namespace;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer serverId;

    private String dataSourceUrl = "mongodb://localhost:27017/?authSource=admin";

    private String driverClassName;

    private boolean deletable = false;

    private volatile boolean active = false;
    /**
     * 版本号，避免config状态变更错误
     */
    private Integer version = 0;

    private String statusKey = "status";

    private String clientSetKey = "clientSet";
    /**
     * 订阅的数据源类型
     */
    private String dataSourceType = "MongoDB";


    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getStatusKey() {
        return statusKey;
    }

    public void setStatusKey(String statusKey) {
        this.statusKey = statusKey;
    }

    public String getClientSetKey() {
        return clientSetKey;
    }

    public void setClientSetKey(String clientSetKey) {
        this.clientSetKey = clientSetKey;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDataSourceUrl() {
        return dataSourceUrl;
    }

    public void setDataSourceUrl(String dataSourceUrl) {
        this.dataSourceUrl = dataSourceUrl;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
