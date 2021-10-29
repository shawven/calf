package com.github.shawven.calf.payment.domain;

/**
 * @author Shoven
 * @date 2019-09-05
 */
public class PaymentTradeCallbackResponse extends PaymentResponse {

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
     * 回复给上游的消息
     */
    private String replayMessage = "";

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

    public String getReplayMessage() {
        return replayMessage;
    }

    public void setReplayMessage(String replayMessage) {
        this.replayMessage = replayMessage;
    }

    @Override
    public String toString() {
        return "PaymentTradeCallbackResponse{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", amount='" + amount + '\'' +
                ", replayMessage='" + replayMessage + '\'' +
                '}';
    }
}
