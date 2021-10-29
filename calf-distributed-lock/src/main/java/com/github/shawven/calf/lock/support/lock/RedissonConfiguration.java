package com.github.shawven.calf.lock.support.lock;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class RedissonConfiguration {

    @Value("${jsy.cloudwork.single.redis.ip}")
    private String singleIp;
    @Value("${jsy.cloudwork.single.redis.port}")
    private String singlePort;
    @Value("${jsy.cloudwork.single.redis.password}")
    private String singlePassword;

    @Value("${spring.redis.sentinel.master:}")
    private String master;
    @Value("${spring.redis.sentinel.nodes:}")
    private String nodes;
    @Value("${spring.redis.sentinel.password:}")
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 为了匹配原有分布式锁实现依赖的RedisTemp配置，此处根据RedisTemp的相关配置条件构建
        if (StringUtils.isNotBlank(singleIp)) {
            SingleServerConfig singleServer = config.useSingleServer();
            singleServer.setAddress("redis://" + singleIp + ":" + singlePort);
            if (StringUtils.isNotBlank(singlePassword)) {
                singleServer.setPassword(singlePassword);
            }
        } else {
            SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
            sentinelServersConfig.setMasterName(master);
            Arrays.stream(StringUtils.split(nodes, ",")).forEach(node -> {
                sentinelServersConfig.addSentinelAddress("redis://" + node);
            });
            if (StringUtils.isNotBlank(password)) {
                sentinelServersConfig.setPassword(password);
            }
        }
        return Redisson.create(config);
    }
}
