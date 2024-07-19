package com.github.shawven.calf.util;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xw
 * @date 2023/11/10
 */
public class ParallelExecutor {

    /**
     * 可以保证多线程时附带用户请求上下文身份信息
     */
    private RequestCtx ctx;

    /**
     * 任务列表
     */
    private final List<Runnable> tasks = new ArrayList<>();

    /**
     * 并发数，大于0，默认16
     */
    private int parallelSize = 16;

    public ParallelExecutor() {}

    /**
     * @param initContext  初始化上下文
     */
    public ParallelExecutor(boolean initContext) {
        if (initContext) {
            ctx = RequestCtx.copyContext();
        }
    }

    /**
     * @param initContext  初始化上下文
     * @param parallelSize 并行数量
     */
    public ParallelExecutor(boolean initContext, int parallelSize) {
        if (initContext) {
            ctx = RequestCtx.copyContext();
        }
        if (parallelSize > 0) {
            this.parallelSize = parallelSize;
        }
    }

    /**
     * 添加任务
     *
     * @param task 任务
     * @return
     */
    public ParallelExecutor add(Runnable task) {
        tasks.add(wrapTask(task));
        return this;
    }

    /**
     * 并发执行，会阻塞当前线程，直到所有任务执行完成
     */
    public void execute() {
        for (List<Runnable> taskList : Lists.partition(tasks, parallelSize)) {
            ThreadPoolUtils.allOf(taskList).join();
        }
    }

    /**
     * 包装任务附带用户请求上下文
     *
     * @param task 任务
     * @return
     */
    private Runnable wrapTask(Runnable task) {
        if (ctx == null) {
            return task;
        }
        // 附带用户请求上下文
        return () -> AttachContext.exec(ctx, task);
    }
}
