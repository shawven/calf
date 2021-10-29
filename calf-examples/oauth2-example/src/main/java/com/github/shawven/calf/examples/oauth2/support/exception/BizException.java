package com.github.shawven.calf.examples.oauth2.support.exception;

/**
 * 业务异常，由客户端导致（用户操作不当、参数错误等）
 * 通过统一拦截拦截器处理返回，不记录日志系统，直接给出错误提示
 *
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

    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

}
