package com.github.shawven.calf.oplog.server.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @auther: chenjh
 * @time: 2018/11/19 9:01
 * @description
 */
@ConfigurationProperties(prefix = "spring.rabbit")
public class RabbitProperties {

    private String host;

    private String port;

    private String username;

    private String password;

    private String virtualHost;

    private String apiUrl;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
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

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
