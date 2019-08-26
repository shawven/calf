package com.test.log.repository;

import com.test.log.core.LogRepository;
import com.test.log.core.Recordable;
import com.test.log.annotation.Log;

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
