package com.test.payment.supplier.unionpay.sdk.request;

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

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeSubType() {
        return tradeSubType;
    }

    public void setTradeSubType(String tradeSubType) {
        this.tradeSubType = tradeSubType;
    }

    @Override
    public String toString() {
        return "UnionpayTradeQueryRequest{" +
                "outTradeNo='" + outRefundNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", tradeSubType='" + tradeSubType + '\'' +
                '}';
    }
}
