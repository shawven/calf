package com.test.support.exception;

/**
 * 系统表示异常
 * 继承于运行时异常，非人为的系统运行时异常
 * 通过统一拦截拦截器处理返回，必须记入日志系统，不应该将信息展示给用户
 *
 * @author Shoven
 * @date 2019-07-11 16:45
 */
public class ServerException extends RuntimeException {

    public ServerException() {
        super();
    }

    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
