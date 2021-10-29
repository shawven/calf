package com.github.shawven.calf.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-05
 */
public class PaymentTradeRefundQueryResponse extends PaymentResponse {

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
     * 平台退款号
     */
    private String refundNo;

    /**
     * 退款金额
     */
    private String refundAmount;

    /**
     * 订单金额
     */
    private String totalAmount;

    /**
     * 交易是否存在
     */
    private boolean notExist;

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

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isNotExist() {
        return notExist;
    }

    public void setNotExist(boolean notExist) {
        this.notExist = notExist;
    }

    @Override
    public String toString() {
        return "PaymentTradeRefundQueryResponse{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", outRefundNo='" + outRefundNo + '\'' +
                ", refundNo='" + refundNo + '\'' +
                ", refundAmount='" + refundAmount + '\'' +
                ", totalAmount='" + totalAmount + '\'' +
                ", notExist=" + notExist +
                "} " + super.toString();
    }
}
