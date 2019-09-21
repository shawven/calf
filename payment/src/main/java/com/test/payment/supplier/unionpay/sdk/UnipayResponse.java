package com.test.payment.supplier.unionpay.sdk;

import com.test.payment.domain.PaymentResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-09-18
 */
public class UnipayResponse {

    private boolean success;

    private String errorMsg;

    private Map<String, String> body;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    public String getBody(String key) {
        return body.get(key);
    }

    public UnipayResponse putBody(String key, String value) {
        Map<String, String> body = getBody();
        if (body == null) {
            body = new HashMap<>();
            body.put(key, value);
            setBody(body);
        } else {
            body.put(key, value);
        }
        return this;
    }
}
