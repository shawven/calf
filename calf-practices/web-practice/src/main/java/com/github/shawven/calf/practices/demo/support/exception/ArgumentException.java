package com.github.shawven.calf.practices.demo.support.exception;

/**
 * @author Shoven
 * @date 2020-03-23
 */
public class ArgumentException extends RuntimeException {

    public ArgumentException() {
        super();
    }

    public ArgumentException(String message) {
        super(message);
    }

    public ArgumentException(Throwable cause) {
        super(cause);
    }

    public ArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

}
