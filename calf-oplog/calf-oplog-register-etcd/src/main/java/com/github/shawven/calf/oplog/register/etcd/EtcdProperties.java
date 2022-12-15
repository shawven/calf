package com.github.shawven.calf.oplog.register.etcd;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/8/2
 **/
@Data
@ConfigurationProperties("spring.etcd")
public class EtcdProperties {

    private List<String> endpoints;

    private String authority;

    private String username;

    private String password;
}
