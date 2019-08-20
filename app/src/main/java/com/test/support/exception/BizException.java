package com.test.support.exception;

/**
 * 业务异常：
 * 在系统允许的、主动发起的内部运行时业务异常，由为用户操作不当导致的
 * 通过统一拦截拦截器处理返回，可不记录日志系统
 * @author Shoven
 * @date  2018-11-09
 */
public class BizException extends RuntimeException {

    public BizException() {
        super();
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
