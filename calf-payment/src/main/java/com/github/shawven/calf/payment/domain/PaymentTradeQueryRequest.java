package com.github.shawven.calf.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-03
 */
public class PaymentTradeQueryRequest extends PaymentRequest {

    /**
     * 商户订单号 （一般情况下和交易号二选一即可）
     */
    private String outTradeNo;

    /**
     * 平台交易号
     */
    private String tradeNo;

    public PaymentTradeQueryRequest(PaymentRequest request) {
        super(request);
    }

    public PaymentTradeQueryRequest(String paymentProvider) {
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

    @Override
    public String toString() {
        return "PaymentTradeQueryRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                '}';
    }
}
