package com.test.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-06
 */
public class PaymentTradeRefundRequest extends PaymentRequest {

    /**
     * 支付交易号
     */
    private String outTradeNo;

    /**
     * 退款交易号
     */
    private String outRefundNo;

    /**
     * 总金额
     */
    private String totalAmount;

    /**
     * 退款金额
     */
    private String refundAmount;

    public PaymentTradeRefundRequest(String paymentSupplier) {
        super(paymentSupplier);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
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

    @Override
    public String toString() {
        return "PaymentTradeRefundRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", outRefundNo='" + outRefundNo + '\'' +
                ", totalAmount='" + totalAmount + '\'' +
                ", refundAmount='" + refundAmount + '\'' +
                '}';
    }
}
