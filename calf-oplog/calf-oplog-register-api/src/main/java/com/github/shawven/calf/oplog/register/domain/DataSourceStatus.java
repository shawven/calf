package com.github.shawven.calf.oplog.register.domain;


import lombok.Data;

import java.util.Set;

/**
 * @author wanglaomo
 * @since 2019/10/18
 **/
@Data
public class DataSourceStatus {

    private String namespace;

    private String filename;

    private long position;

    private long timestamp;

    private String dateTime;
}
