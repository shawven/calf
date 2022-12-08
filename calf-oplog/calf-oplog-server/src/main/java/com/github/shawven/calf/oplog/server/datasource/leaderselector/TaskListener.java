package com.github.shawven.calf.oplog.server.datasource.leaderselector;

/**
 * 任务监听器
 *
 * @author xw
 * @date 2022-12-08
 */
public interface TaskListener {

    /**
     * 任务开始
     */
    void start() throws InterruptedException;

    /**
     * 任务结束
     */
    void end();
}
