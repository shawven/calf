package com.example.zkpractice;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class ZkPracticeApplicationTests {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected CuratorFramework client;


}
