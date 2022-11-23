package com.example.feignpractice;

import com.example.feignpractice.config.Dict;
import com.example.feignpractice.config.RequestWith;
import feign.Retryer;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xw
 * @date 2022/10/20
 */
@FeignClient(name = "cloudAuth", url = "https://api.github.com", configuration = GithubFeignClient.CloudAuthFeignConfiguration.class)
public interface GithubFeignClient {

    @GetMapping("/repos/shawven/calf/git/trees/master")
    @RequestWith(queries = @Dict(name = "recursive", value = "1"),
            readTimeoutMillis = 6000, connectTimeoutMillis = 6000)
    RepoResult repos();


    @Configuration
    class CloudAuthFeignConfiguration {

        @Bean
        public Retryer retryer() {
            return new Retryer.Default(100, 1000, 3);
        }
    }


    @Data
    class RepoResult {
        String sha;
        String url;
        List<Node> tree = new ArrayList<>();
    }

    @Data
    class Node {
        private String path;
        private String mode;
        private String type;
        private String sha;
        private int size;
        private String url;
    }

}
