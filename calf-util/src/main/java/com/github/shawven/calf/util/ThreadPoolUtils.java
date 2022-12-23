package com.github.shawven.calf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolUtils.class);

    /**
     * 默认线程池
     *
     * @return
     */
    public static Ops opsForDefault() {
        return DefaultExecutorOps.INSTANCE;
    }

    /**
     * 第三方线程池
     *
     * @return
     */
    public static Ops opsForThird() {
        return ThirdExecutorOps.INSTANCE;
    }

    /**
     * 默认线程池快捷方法 execute
     *
     * @param command
     */
	public static void execute(Runnable command) {
        opsForDefault().execute(command);
	}

    /**
     * 默认线程池快捷方法 submit
     *
     * @param task
     * @return
     */
    public static Future<?> submit(Runnable task) {
        return opsForDefault().submit(task);
    }

    /**
     * 默认线程池快捷方法 submit
     *
     * @param task
     * @param <T>
     * @return
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return opsForDefault().submit(task);
    }

    /**
     * 默认线程池快捷方法 submit
     *
     * @param task
     * @return
     */
    public static ListenableFuture<?> submitListenable(Runnable task) {
        return opsForDefault().submitListenable(task);
    }

    /**
     * 默认线程池快捷方法 submit
     *
     * @param task
     * @param <T>
     * @return
     */
    public static <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return opsForDefault().submitListenable(task);
    }

    /**
     * 默认线程池快捷方法 submit
     *
     * @return
     */
    public static ThreadPoolTaskExecutor getExecutor() {
        return opsForDefault().getExecutor();
    }

    private static ThreadPoolTaskExecutor newExecutor(String threadPrefix, int corePoolSize, int maxPoolSize, int queueSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 设置队列容量
        executor.setQueueCapacity(queueSize);
        // 设置拒绝策略rejection-policy：当pool已经达到max size的时候，如何处理新任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 任务装饰器
        executor.setTaskDecorator(ThreadPoolUtils::wrap);
        // 设置默认线程名称
        executor.setThreadFactory(newThreadFactory(threadPrefix));
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

    public static CustomizableThreadFactory newThreadFactory(String prefix) {
        return new CustomizableThreadFactory(prefix) {
            private static final long serialVersionUID = -8293867781666698804L;

            @Override
            public Thread createThread(Runnable runnable) {
                Thread thread = super.createThread(runnable);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    LOGGER.error(e.getMessage(), e);
                });
                return thread;
            }
        };
    }


    /**
     * 高优先级 并发io任务为主，尽可能多的线程
     */
    private static class DefaultExecutorOps implements Ops {

        static final Ops INSTANCE = new DefaultExecutorOps();

        private static final String THREAD_PREFIX = "io-task-";
        private static final int DEFAULT_CORE_POOL_SIZE = 500;
        private static final int DEFAULT_MAX_POOL_SIZE = 1000;
        private static final int DEFAULT_QUEUE_SIZE = 30;
        private static final ThreadPoolTaskExecutor DEFAULT_EXECUTOR =
                newExecutor(THREAD_PREFIX, DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, DEFAULT_QUEUE_SIZE);

        @Override
        public ThreadPoolTaskExecutor getExecutor() {
            return DEFAULT_EXECUTOR;
        }
    }

    /**
     * 低优先级 外部第三方io任务，尽可能大的队列
     */
    private static class ThirdExecutorOps implements Ops {

        static final Ops INSTANCE = new ThirdExecutorOps();

        private static final String THREAD_PREFIX = "tio-task-";
        private static final int THIRD_CORE_POOL_SIZE = 5;
        private static final int THIRD_MAX_POOL_SIZE = 5;
        private static final int THIRD_QUEUE_SIZE = 100000;
        private static final ThreadPoolTaskExecutor THIRD_EXECUTOR =
                newExecutor(THREAD_PREFIX, THIRD_CORE_POOL_SIZE, THIRD_MAX_POOL_SIZE, THIRD_QUEUE_SIZE);

        @Override
        public ThreadPoolTaskExecutor getExecutor() {
            return THIRD_EXECUTOR;
        }
    }

    public interface Ops {

        ThreadPoolTaskExecutor getExecutor();

        default void execute(Runnable command) {
            getExecutor().execute(command);
        }

        default Future<?> submit(Runnable task) {
            return getExecutor().submit(task);
        }

        default <T> Future<T> submit(Callable<T> task) {
            return getExecutor().submit(task);
        }

        default ListenableFuture<?> submitListenable(Runnable task) {
            return getExecutor().submitListenable(task);
        }

        default <T>  ListenableFuture<T> submitListenable(Callable<T> task) {
            return getExecutor().submitListenable(task);
        }
    }
}
