package com.example.etcdpratice;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.support.Util;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(EtcdProperties.class)
@SpringBootApplication
public class EtcdPraticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtcdPraticeApplication.class, args);
    }

    @Bean
    public Client etcdClient(EtcdProperties etcdProperties) {
        return Client
                .builder()
                .endpoints(Util.toURIs(etcdProperties.getEndpoints()))
                //.authority(authority)
                //.user(ByteSequence.from(username, StandardCharsets.UTF_8))
                //.password(ByteSequence.from(password, StandardCharsets.UTF_8))
                .build();
    }
}
