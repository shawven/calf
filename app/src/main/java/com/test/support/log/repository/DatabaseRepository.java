package com.test.support.log.repository;

import com.test.support.log.annotation.Log;
import com.test.support.log.core.LogRepository;
import com.test.support.log.core.Recordable;

/**
 * @author Shoven
 * @date 2019-07-26 15:04
 */
public class DatabaseRepository implements LogRepository {

    @Override
    public boolean isSupport(Log annotation) {
        return true;
    }

    @Override
    public void write(Recordable record) {
        System.out.println("写入数据库");
        System.out.println(record.toString());
    }
}
