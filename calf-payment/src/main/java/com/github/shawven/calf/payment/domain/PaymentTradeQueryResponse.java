package com.github.shawven.calf.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-05
 */
public class PaymentTradeQueryResponse extends PaymentResponse {

    /**
     * 商户交易号
     */
    private String outTradeNo;

    /**
     * 平台交易号
     */
    private String tradeNo;

    /**
     * 金额
     */
    private String amount;

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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isNotExist() {
        return notExist;
    }

    public void setNotExist(boolean notExist) {
        this.notExist = notExist;
    }

    @Override
    public String toString() {
        return "PaymentTradeQueryResponse{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", amount='" + amount + '\'' +
                ", notFound=" + notExist +
                "} " + super.toString();
    }
}
