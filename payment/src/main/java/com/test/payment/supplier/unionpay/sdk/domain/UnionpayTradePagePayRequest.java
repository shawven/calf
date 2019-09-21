package com.test.payment.supplier.unionpay.sdk.domain;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradePagePayRequest extends UnionpayTradeRequest {

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
     * 交易子类型  01：自助消费
     */
    private String tradeSubType = "01";

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

    public String getTradeSubType() {
        return tradeSubType;
    }

    @Override
    public String toString() {
        return "UnionpayTradePagePayRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", amount=" + amount +
                ", subject='" + subject + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", tradeSubType='" + tradeSubType + '\'' +
                '}';
    }
}
