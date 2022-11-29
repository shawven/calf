package com.github.shawven.calf.oplog.base;


import lombok.Data;

import java.util.Set;

/**
 * @author wanglaomo
 * @since 2019/10/18
 **/
@Data
public class ServiceStatus {

    private String ip;

    private Set<String> activeNamespaces;

    private long totalEventCount;

    private long latelyEventCount;

    private long totalPublishCount;

    private long latelyPublishCount;

    private String updateTime;
}
