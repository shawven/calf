package com.github.shawven.calf.payment.domain;

import com.github.shawven.calf.payment.support.PaymentUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-09-05
 */
public class PaymentTradeCallbackRequest extends PaymentRequest {

    /**
     * Map参数
     */
    private Map<String, String> params;

    /**
     * 原始请求体
     */
    private String rowBody;

    public PaymentTradeCallbackRequest(String paymentProvider, Map<String, ?> params, InputStream inputStream) {
        super(paymentProvider);
        this.rowBody = PaymentUtils.read(inputStream);
        this.params = getParams(params, rowBody);
    }

    private Map<String, String> getParams(Map<String, ?> formParams, String str) {
        params = PaymentUtils.parseParameterMap(formParams);
        if (params.isEmpty()) {
            params = PaymentUtils.splitPairString(str);
        }
        return params;
    }

    public Map<String, String> getParams() {
        return new HashMap<>(params);
    }

    public String get(String key) {
        return params.get(key);
    }

    public String getRowBody() {
        return rowBody;
    }

    @Override
    public String toString() {
        return "PaymentTradeCallbackRequest{" +
                "params=" + getParams().toString() +
                '}';
    }
}
