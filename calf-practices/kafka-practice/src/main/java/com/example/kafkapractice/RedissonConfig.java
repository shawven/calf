package com.example.kafkapractice;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xw
 * @date 2024/7/23
 */
@Configuration(proxyBeanMethods = false)
public class RedissonConfig {

    @Autowired
    private RedisProperties properties;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        RedisProperties.Cluster cluster = properties.getCluster();
        String[] nodes = cluster.getNodes().stream().map(s -> "redis://" + s).toArray(String[]::new);

        ClusterServersConfig clusterServers = config.useClusterServers();
        clusterServers.addNodeAddress(nodes);

        if (properties.getPassword() != null) {
            clusterServers.setPassword(properties.getPassword());
        }
        return Redisson.create(config);
    }
}
