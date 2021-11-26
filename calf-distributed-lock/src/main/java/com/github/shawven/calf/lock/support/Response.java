package com.github.shawven.calf.lock.support;

/**
 * 自定义响应消息体
 * 提供一些静态方法封装 ResponseEntity 以适应RestFull风格API，会改变http status
 *
 * @author Shoven
 * @date 2019-07-10 14:27
 */
public class Response {
    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private Object data;

    public Response(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
