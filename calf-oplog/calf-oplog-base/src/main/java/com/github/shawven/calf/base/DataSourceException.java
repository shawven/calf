package com.github.shawven.calf.base;

/**
 * @author xw
 * @date 2021/11/15
 */
public class DataSourceException extends RuntimeException{

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
