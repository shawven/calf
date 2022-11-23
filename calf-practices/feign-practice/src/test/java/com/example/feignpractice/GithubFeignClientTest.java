package com.example.feignpractice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author xw
 * @date 2022/11/23
 */
class GithubFeignClientTest extends FeignPracticeApplicationTests{

    @Autowired
    private GithubFeignClient githubFeignClient;

    @Test
    public void test() {
        GithubFeignClient.RepoResult repos = githubFeignClient.repos();
        assertTrue(!repos.getTree().isEmpty());
    }

}
