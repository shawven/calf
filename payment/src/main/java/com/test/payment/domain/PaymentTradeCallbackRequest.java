package com.test.payment.domain;

import com.test.payment.support.PaymentUtils;

import java.io.InputStream;
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
     * 元素输入流
     */
    private InputStream inputStream;

    public PaymentTradeCallbackRequest(String paymentSupplier, Map<String, ?> params, InputStream inputStream) {
        super(paymentSupplier);
        this.params = PaymentUtils.parseParameterMap(params);
        this.inputStream = inputStream;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public String toString() {
        return "PaymentTradeCallbackRequest{" +
                "params=" + params +
                ", inputStream=" + inputStream +
                '}';
    }
}
