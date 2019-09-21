package com.test.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-06
 */
public class PaymentTradeRefundResponse extends PaymentResponse {

    /**
     * 商户交易号
     */
    private String outTradeNo;

    /**
     * 平台交易号
     */
    private String tradeNo;

    /**
     * 退款金额
     */
    private String refundAmount;


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


    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    @Override
    public String toString() {
        return "PaymentTradeRefundResponse{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                '}';
    }
}
