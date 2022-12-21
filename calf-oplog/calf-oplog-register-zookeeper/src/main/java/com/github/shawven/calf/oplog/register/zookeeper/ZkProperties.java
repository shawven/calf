package com.github.shawven.calf.oplog.register.zookeeper;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/8/2
 **/
@Data
@ConfigurationProperties("calf-oplog.zookeeper")
public class ZkProperties {

    private String url;

    private String root;
}
