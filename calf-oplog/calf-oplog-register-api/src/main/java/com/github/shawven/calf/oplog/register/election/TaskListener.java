package com.github.shawven.calf.oplog.register.election;

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
    void start();

    /**
     * 任务结束
     */
    void end();
}
