package com.starter.log.repository;

import com.starter.log.core.JoinPointInfo;
import com.starter.log.core.LogRepository;
import com.starter.log.core.Recordable;

/**
 * @author Shoven
 * @date 2019-07-26 15:04
 */
public class DatabaseRepository implements LogRepository {

    @Override
    public boolean isSupport(JoinPointInfo joinPointInfo) {
        return true;
    }

    @Override
    public void write(Recordable record) {
        System.out.println("写入数据库");
        System.out.println(record.toString());
    }
}
