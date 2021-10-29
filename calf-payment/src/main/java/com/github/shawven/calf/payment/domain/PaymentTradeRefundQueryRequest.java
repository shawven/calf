package com.github.shawven.calf.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-03
 */
public class PaymentTradeRefundQueryRequest extends PaymentRequest {

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

    public PaymentTradeRefundQueryRequest(PaymentRequest request) {
        super(request);
    }

    public PaymentTradeRefundQueryRequest(String paymentProvider) {
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

    @Override
    public String toString() {
        return "PaymentTradeRefundQueryRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", outRefundNo='" + outRefundNo + '\'' +
                '}';
    }
}
