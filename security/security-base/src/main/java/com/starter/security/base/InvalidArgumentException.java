package com.starter.security.base;


import org.springframework.security.core.AuthenticationException;

/**
 * @author Shoven
 * @since 2019-04-24 15:42
 */
public class InvalidArgumentException extends AuthenticationException {

    public InvalidArgumentException(String msg) {
        super(msg);
    }

    public InvalidArgumentException(String msg, Throwable t) {
        super(msg, t);
    }
}
