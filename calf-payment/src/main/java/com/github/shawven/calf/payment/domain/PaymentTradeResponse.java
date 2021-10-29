package com.github.shawven.calf.payment.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-09-05
 */
public class PaymentTradeResponse extends PaymentResponse<Map<String, String>> {

    /**
     * 交易完成，当面付(付款码)这种会直接完成支付
     */
    private boolean tradeSuccess;

    /**
     * 商户交易号
     */
    public String getOutTradeNo() {
        return getBody().get("outTradeNo");
    }

    public void setOutTradeNo(String outTradeNo) {
        putBody("outTradeNo", outTradeNo);
    }

    /**
     * 平台交易号
     */
    public String getTradeNo() {
        return getBody().get("tradeNo");
    }

    public void setTradeNo(String tradeNo) {
        putBody("tradeNo", tradeNo);
    }

    /**
     * form表单
     */
    public String getForm() {
        return getBody().get("form");
    }

    public void setForm(String form) {
        putBody("form", form);
    }

    /**
     * 跳转url
     */
    public String getUrl() {
        return getBody().get("url");
    }

    public void setUrl(String url) {
        putBody("url", url);
    }

    /**
     * 二维码url
     */
    public String getCodeUrl() {
        return getBody().get("codeUrl");
    }

    public void setCodeUrl(String codeUrl) {
        putBody("codeUrl", codeUrl);
    }

    /**
     * 预支付ID
     */
    public String getPrepayId() {
        return getBody().get("prepayId");
    }

    public void setPrepayId(String prepayId) {
        putBody("prepayId", prepayId);
    }

    public boolean isTradeSuccess() {
        return tradeSuccess;
    }

    public void setTradeSuccess(boolean tradeSuccess) {
        this.tradeSuccess = tradeSuccess;
    }

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
