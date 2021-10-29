package com.github.shawven.calf.payment.provider.unionpay.sdk;

/**
 * @author Shoven
 * @date 2019-09-18
 */
public class UnionpayException extends Exception {
    public UnionpayException(String message) {
        super(message);
    }

    public UnionpayException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnionpayException(Throwable cause) {
        super(cause);
    }
}
