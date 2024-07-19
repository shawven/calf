package com.github.shawven.calf.util;

import lombok.Data;

/**
 * 请求上下文对象
 *
 * @author xw
 * @date 2023/6/9
 */
@Data
public class RequestCtx  {

    /**
     * 复制上下文生成请求上下文对象
     *
     * @return
     */
    public static RequestCtx copyContext() {
        RequestCtx requestCtx = new RequestCtx();
        // do something
        return requestCtx;
    }

}
