package com.github.shawven.calf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;

public class ScheduleThreadPoolUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleThreadPoolUtils.class);
	private static final ThreadPoolTaskScheduler EXECUTOR = newExecutor();

    public static ScheduledFuture<?> schedule(Runnable task, Date startTime) {
        return EXECUTOR.schedule(wrap(task), startTime);
    }

    public static ScheduledFuture<?> schedule(Runnable task, long delay) {
        return EXECUTOR.schedule(wrap(task), Date.from(Instant.now().plusMillis(delay)));
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        return EXECUTOR.scheduleAtFixedRate(task, startTime, period);
    }


    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
        return EXECUTOR.scheduleAtFixedRate(task, period);
    }


    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        return EXECUTOR.scheduleWithFixedDelay(task, startTime, delay);
    }


    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
        return EXECUTOR.scheduleWithFixedDelay(task, delay);
    }

    public static ThreadPoolTaskScheduler getExecutor() {
        return EXECUTOR;
    }

    private static ThreadPoolTaskScheduler newExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        // 设置线程数
        executor.setPoolSize(Runtime.getRuntime().availableProcessors());
        // 设置拒绝策略rejection-policy：当pool已经达到max size的时候，如何处理新任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 任务装饰器
        executor.setErrorHandler(e -> LOGGER.error(e.getMessage(), e));
        // 设置默认线程名称
        executor.setThreadNamePrefix("schedule-task-");
        // 初始化
        executor.initialize();
        return executor;
    }

    private static Runnable wrap(final Runnable runnable) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            if (MDC.get("traceId") == null) {
                MDC.put("traceId", UUID.randomUUID().toString());
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
