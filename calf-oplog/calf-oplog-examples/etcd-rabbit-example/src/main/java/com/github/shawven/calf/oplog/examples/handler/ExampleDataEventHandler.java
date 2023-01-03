package com.github.shawven.calf.oplog.examples.handler;

import com.github.shawven.calf.oplog.base.EventAction;
import com.github.shawven.calf.oplog.client.DataSubscribeHandler;
import com.github.shawven.calf.oplog.client.annotation.DataSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * @auther: chenjh
 * @time: 2018/10/16 11:32
 * @description
 * database: mysql库名
 * table: 表名
 * events 需要监听的数操作集合
 * LockLevel为保持顺序的级别,None为默认
 *   TABLE -> 同表按顺序执行
 *   COLUMN -> 某列值一致的按顺序执行
 *   NONE -> 无序
 */
@Service
public class ExampleDataEventHandler implements DataSubscribeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleDataEventHandler.class);

    @Override
    public String key() {
        return "default_test_t_user";
    }

    @Override
    public EventAction[] actions() {
        return new EventAction[]{EventAction.INSERT, EventAction.UPDATE, EventAction.DELETE};
    }

    /**
     * 此方法处理变更信息
     * @param data
     */
    @Override
    public void handle(String data) {
        LOGGER.info("接收信息:" + data);
    }



    @DataSubscriber(
            database = "test",
            table = "t_user",
            actions = {EventAction.INSERT, EventAction.UPDATE, EventAction.DELETE})
    public void handle2(String data) {
        LOGGER.info("接收信息:" + data);
    }
}
