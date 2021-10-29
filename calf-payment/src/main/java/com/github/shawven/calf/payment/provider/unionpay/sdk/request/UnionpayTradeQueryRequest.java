package com.github.shawven.calf.payment.provider.unionpay.sdk.request;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradeQueryRequest extends UnionpayTradeRequest {

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 查询交易号
     */
    private String tradeNo;

    /**
     * 交易类型
     */
    private String tradeType = "00";

    /**
     * 交易子类型
     */
    private String tradeSubType = "00";

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
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

    @Override
    public String toString() {
        return "UnionpayTradeQueryRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", tradeSubType='" + tradeSubType + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", accessType='" + accessType + '\'' +
                ", bizType='" + bizType + '\'' +
                "} " + super.toString();
    }
}
