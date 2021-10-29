package com.github.shawven.calf.payment.provider.unionpay.sdk.request;

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
     * 交易类型
     */
    private String tradeType;

    /**
     * 交易子类型
     */
    private String tradeSubType;

    /**
     * 渠道类型
     */
    private String channelType;

    /**
     * 授权码（付款码支付）
     */
    private String authCode;

    /**
     * 终端ID（付款码支付）
     */
    private String termId;

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

    @Override
    public String getTradeType() {
        return tradeType;
    }

    @Override
    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    @Override
    public String getTradeSubType() {
        return tradeSubType;
    }

    @Override
    public void setTradeSubType(String tradeSubType) {
        this.tradeSubType = tradeSubType;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
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
                ", tradeSubType='" + tradeSubType + '\'' +
                ", channelType='" + channelType + '\'' +
                ", authCode='" + authCode + '\'' +
                ", termId='" + termId + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", accessType='" + accessType + '\'' +
                ", bizType='" + bizType + '\'' +
                "} " + super.toString();
    }
}
