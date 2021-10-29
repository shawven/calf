package com.github.shawven.calf.payment.domain;

import java.io.Serializable;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public class PaymentResponse<T> implements Serializable {

    private boolean success;

    private String state;

    private String errorMsg;

    private T body;

    public boolean isSuccess() {
        return success;
    }

    public PaymentResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public PaymentResponse setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public T getBody() {
        return body;
    }

    public PaymentResponse setBody(T body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "success=" + success +
                ", message='" + errorMsg + '\'' +
                ", body=" + body +
                '}';
    }
}
