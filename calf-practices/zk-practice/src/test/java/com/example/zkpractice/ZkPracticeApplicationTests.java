package com.example.zkpractice;

import org.apache.curator.RetryLoop;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class ZkPracticeApplicationTests {

    private final Logger logger = LoggerFactory.getLogger(ZkPracticeApplicationTests.class);

    @Autowired
    private CuratorFramework client;

    private String path = "/default/test";

    @Test
    @Order(Integer.MIN_VALUE)
    public void watch() {
        CuratorCache curatorCache = CuratorCache.build(client, path, CuratorCache.Options.DO_NOT_CLEAR_ON_CLOSE);
        curatorCache.listenable().addListener((type, oldData, data) -> {
            logger.info("type:{}, oldData:{}, data:{}", type, oldData, data);
        });
        curatorCache.start();
    }


    @Test
    @Order(1)
    void create() throws Exception {
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path + "/hello", "world".getBytes(StandardCharsets.UTF_8));

    }

    @Test
    @Order(2)
    void get() throws Exception {
        byte[] bytes = client.getData().forPath(path + "/hello");
        logger.info(new String(bytes));
    }

    @Test
    @Order(4)
    void getAll() throws Exception {
        List<String> paths = client.getChildren().forPath(path);
        for (String childPath : paths) {
            byte[] bytes = client.getData().forPath(path + "/" + childPath);
            logger.info(new String(bytes));
        }
    }

    @Test
    @Order(2)
    void update() throws Exception {
        client.setData()
                .forPath(path + "/hello", "update hello world".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @Order(3)
    void updateNotExist() throws Exception {
        String fullPath = path + "/notExist";
        Stat stat = client.checkExists().forPath(fullPath);
        if (stat != null) {
            try {
                stat = client.setData()
                        .withVersion(stat.getVersion())
                        .forPath(fullPath, LocalTime.now().toString().getBytes(StandardCharsets.UTF_8));
                logger.info("update success: {}", stat);
            } catch (KeeperException e) {
                if (e.code() == KeeperException.Code.BADVERSION) {
                    logger.warn("retry update");
                }
                logger.error("update error");
            }
        } else {
            try {
                String s = client.create()
                        .forPath(fullPath, "hello world".getBytes(StandardCharsets.UTF_8));
                logger.info("create success: {}", s);
            } catch (KeeperException e) {
                if (e.code() == KeeperException.Code.NODEEXISTS) {
                    logger.warn("retry update");
                }
                logger.error("create error");
            }

        }

    }

    @Test
    @Order(Integer.MAX_VALUE)
    void delete() throws Exception {
        client.delete().forPath(path + "/hello");
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void clean() throws Exception {
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }
}
