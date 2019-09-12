package com.test.payment.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-09-05
 */
public class PaymentTradeResponse extends PaymentResponse<Map<String, String>> {

    public PaymentResponse putBody(String key, String value) {
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
