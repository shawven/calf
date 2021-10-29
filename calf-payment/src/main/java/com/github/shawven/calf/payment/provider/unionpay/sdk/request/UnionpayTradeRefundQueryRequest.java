package com.github.shawven.calf.payment.provider.unionpay.sdk.request;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradeRefundQueryRequest extends UnionpayTradeRequest {

    /**
     * 退款单号
     */
    private String outRefundNo;

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
        return "UnionpayTradeRefundQueryRequest{" +
                "outRefundNo='" + outRefundNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", tradeSubType='" + tradeSubType + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", accessType='" + accessType + '\'' +
                ", bizType='" + bizType + '\'' +
                "} " + super.toString();
    }
}
