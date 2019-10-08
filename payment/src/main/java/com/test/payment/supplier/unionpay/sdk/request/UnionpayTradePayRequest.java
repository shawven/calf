package com.test.payment.supplier.unionpay.sdk.request;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradePayRequest extends UnionpayTradeRequest {

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 金额分
     */
    private String amount;

    /**
     * 商品主题
     */
    private String subject;

    /**
     * 前台跳转地址
     */
    private String returnUrl;

    /**
     * 后台通知地址
     */
    private String notifyUrl;


    /**
     * 交易类型 01：消费
     */
    private String tradeType = "01";

    /**
     * 二维码授权码
     */
    private String authCode;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    @Override
    public String toString() {
        return "UnionpayTradePayRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", amount='" + amount + '\'' +
                ", subject='" + subject + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", authCode='" + authCode + '\'' +
                '}';
    }
}
