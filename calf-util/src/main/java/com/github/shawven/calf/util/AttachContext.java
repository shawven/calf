package com.github.shawven.calf.util;

import java.util.function.Supplier;

/**
 * 附加上下文
 *
 * @author xw
 * @date 2023/3/6
 */
public class AttachContext {


    public static <T> T exec(RequestCtx ctx, Supplier<T> supplier) {
        // 旧上下文
        RequestCtx oldCtx = RequestCtx.copyContext();

        setContext(BeanMaps.map(ctx, RequestCtx.class));
        try {
            return supplier.get();
        } finally {
            // 还原
            setContext(oldCtx);
        }
    }

    public static void exec(RequestCtx ctx, Runnable runnable) {
        // 旧上下文
        RequestCtx oldCtx = RequestCtx.copyContext();

        setContext(BeanMaps.map(ctx, RequestCtx.class));
        try {
            runnable.run();
        } finally {
            // 还原
            setContext(oldCtx);
        }
    }

    private static void setContext(RequestCtx ctx) {
        // do something
    }
}
