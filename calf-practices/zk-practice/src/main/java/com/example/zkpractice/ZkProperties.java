package com.example.zkpractice;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("zookeeper")
public class ZkProperties {

    private String url;

}
