package com.test.payment.supplier.unionpay.sdk.request;

/**
 * @author Shoven
 * @date 2019-10-08
 */
public class UnionpayTradeCancelRequest extends UnionpayTradeRequest  {
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
     * 交易类型 31-消费撤销
     */
    private String tradeType = "31";

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

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    @Override
    public String toString() {
        return "UnionpayTradeCancelRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", amount='" + amount + '\'' +
                ", subject='" + subject + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", tradeType='" + tradeType + '\'' +
                '}';
    }
}
