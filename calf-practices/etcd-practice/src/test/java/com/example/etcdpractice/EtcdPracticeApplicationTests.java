package com.example.etcdpractice;

import io.etcd.jetcd.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class EtcdPracticeApplicationTests {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected Client client;

}
