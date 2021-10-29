package com.github.shawven.calf.payment;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public class PaymentException extends RuntimeException {

    public PaymentException() {
        super();
    }

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentException(Throwable cause) {
        super(cause);
    }
}
