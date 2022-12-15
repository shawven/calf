package com.github.shawven.calf.oplog.server.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @auther: chenjh
 * @time: 2018/11/19 9:01
 * @description
 */
@Data
@ConfigurationProperties(prefix = "spring.rabbit")
public class RabbitProperties {

    private String host;

    private String port;

    private String username;

    private String password;

    private String virtualHost;

    private String apiUrl;
}
