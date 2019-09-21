package com.test.payment.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-08-28
 */
public class PaymentTradeRequest extends PaymentRequest {

    public PaymentTradeRequest(String paymentSupplier, String paymentClient) {
        super(paymentSupplier, paymentClient);
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
     * ip地址
     */
    private String ip;

    private Map<String, String> option;

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

    public Map<String, String> getOption() {
        return option;
    }

    public void setOption(Map<String, String> option) {
        this.option = option;
    }

    public void putOption(String key, String value) {
        Map<String, String> option = getOption();
        if (option == null) {
            option = new HashMap<>();
            option.put(key, value);
            setOption(option);
        } else {
            option.put(key, value);
        }
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
