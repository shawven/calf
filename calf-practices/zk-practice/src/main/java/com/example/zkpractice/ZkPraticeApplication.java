package com.example.zkpractice;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(ZkProperties.class)
@SpringBootApplication
public class ZkPraticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZkPraticeApplication.class, args);
    }

    @Bean
    public CuratorFramework etcdClient(ZkProperties etcdProperties) {
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString(etcdProperties.getUrl())
                .retryPolicy(new RetryNTimes(3, 500))
                .build();

        client.start();
        return client;
    }
}
