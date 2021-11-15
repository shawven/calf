package com.github.shawven.calf.oplog.server.core.leaderselector;

/**
 * @author wanglaomo
 * @since 2019/7/30
 **/
public interface LeaderSelectorListener {

    /**
     * 获取领导权后
     */
    void afterTakeLeadership() throws InterruptedException;

    /**
     * 失去领导权后
     */
    boolean afterLosingLeadership();
}
