package com.github.shawven.calf.payment.provider.unionpay.sdk.request;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradeRefundRequest extends UnionpayTradeRequest {

    /**
     * 订单号
     */
    private String outRefundNo;

    /**
     * 交易好啊
     */
    private String tradeNo;

    /**
     * 退款金额
     */
    private String refundAmount;

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

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
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

    @Override
    public String toString() {
        return "UnionpayTradeRefundRequest{" +
                "outRefundNo='" + outRefundNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", refundAmount='" + refundAmount + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", tradeSubType='" + tradeSubType + '\'' +
                ", channelType='" + channelType + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", accessType='" + accessType + '\'' +
                ", bizType='" + bizType + '\'' +
                "} " + super.toString();
    }
}
