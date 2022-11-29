package com.example.nativepractice.util;

import lombok.Data;

/**
 * @author xw
 * @date 2022/11/25
 */
@Data
public class Result<T> {

    private int code;

    private String msg;

    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(0, null, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(0, null, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
