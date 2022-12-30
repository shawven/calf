package com.github.shawven.calf.oplog.examples.handler;

import com.github.shawven.calf.oplog.base.DatabaseEvent;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.client.DatabaseEventHandler;
import com.github.shawven.calf.oplog.client.HandleDatabaseEvent;
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
@HandleDatabaseEvent(
        namespace = "test",
        database = "test",
        table = "t_user",
        events = {DatabaseEvent.WRITE_ROWS,DatabaseEvent.UPDATE_ROWS, DatabaseEvent.DELETE_ROWS})
public class ExampleDataEventHadler implements DatabaseEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleDataEventHadler.class);

    /**
     * 此方法处理变更信息
     * @param eventBaseDTO
     */
    @Override
    public void handle(EventBaseDTO eventBaseDTO) {
        LOGGER.info("接收信息:" + eventBaseDTO.toString());
    }

    @Override
    public Class getClazz() {
        return ExampleDataEventHadler.class;
    }
}
