package com.github.shawven.calf.examples.oauth2.support.exception;

/**
 * 系统异常，由服务端导致的运行时异常
 * 通过统一拦截拦截器处理返回，必须记入日志系统，且暴露的信息不应该将展示给用户
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

    public ServerException(Throwable cause) {
        super(cause);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
