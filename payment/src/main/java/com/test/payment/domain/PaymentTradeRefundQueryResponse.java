package com.test.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-05
 */
public class PaymentTradeRefundQueryResponse extends PaymentResponse {

    /**
     * 平台退款交易号
     */
    private String refundNo;

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    @Override
    public String toString() {
        return "PaymentTradeRefundQueryResponse{" +
                "refundNo='" + refundNo + '\'' +
                '}';
    }
}
