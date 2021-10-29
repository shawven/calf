package com.github.shawven.calf.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-06
 */
public class PaymentTradeRefundRequest extends PaymentRequest {

    /**
     * 商户交易号
     */
    private String outTradeNo;

    /**
     * 平台交易号
     */
    private String tradeNo;

    /**
     * 商户退款号
     */
    private String outRefundNo;

    /**
     * 订单总金额
     */
    private String totalAmount;

    /**
     * 退款金额
     */
    private String refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    public PaymentTradeRefundRequest(String paymentProvider) {
        super(paymentProvider);
    }

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

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    @Override
    public String toString() {
        return "PaymentTradeRefundRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", outRefundNo='" + outRefundNo + '\'' +
                ", totalAmount='" + totalAmount + '\'' +
                ", refundAmount='" + refundAmount + '\'' +
                ", refundReason='" + refundReason + '\'' +
                '}';
    }
}
