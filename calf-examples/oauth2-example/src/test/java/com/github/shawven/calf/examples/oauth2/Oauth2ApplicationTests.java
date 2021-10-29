package com.github.shawven.calf.examples.oauth2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Oauth2ApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(Oauth2ApplicationTests.class);

    private RedisTemplate<String, Object> redisTemplate;

	@Test
	public void testSpringData() {

    }


}
