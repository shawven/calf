package com.test.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-06
 */
public class PaymentTradeRefundResponse extends PaymentResponse {

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
        return "PaymentTradeRefundResponse{" +
                "refundNo='" + refundNo + '\'' +
                '}';
    }
}
