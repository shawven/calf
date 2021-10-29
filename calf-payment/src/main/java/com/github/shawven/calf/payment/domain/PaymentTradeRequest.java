package com.github.shawven.calf.payment.domain;

import com.github.shawven.calf.payment.support.PaymentUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-08-28
 */
public class PaymentTradeRequest extends PaymentRequest {

    public PaymentTradeRequest(String paymentProvider, String paymentClient, Map<String, ?> formParams) {
        super(paymentProvider, paymentClient);
        params = PaymentUtils.parseParameterMap(formParams);
    }

    /**
     * 商户交易号
     */
    private String outTradeNo;

    /**
     * 商品标题
     */
    private String subject;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 金额
     */
    private String amount;

    /**
     * ip地址（微信需要）
     */
    private String ip;

    private Map<String, String> params;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<String, String> getParams() {
        return new HashMap<>(params);
    }

    public String get(String key) {
        return params.get(key);
    }

    @Override
    public String toString() {
        return "PaymentTradeRequest{" +
                "tradeNo='" + outTradeNo + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", amount='" + amount + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
