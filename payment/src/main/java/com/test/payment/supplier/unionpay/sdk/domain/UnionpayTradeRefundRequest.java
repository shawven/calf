package com.test.payment.supplier.unionpay.sdk.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradeRefundRequest extends UnionpayTradeRequest {

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 交易好啊
     */
    private String tradeNo;

    /**
     * 退款金额
     */
    private String refundAmount;

    /**
     * 后台通知地址
     */
    private String notifyUrl;

    /**
     * 交易类型
     */
    private String tradeType = "04";

    /**
     * 交易子类型
     */
    private String tradeSubType = "00";

    /**
     *  业务类型，B2C网关支付，手机wap支付
     */
    private String bizType = "000201";

    /**
     * 渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
     */
    private String channelType = "07";

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

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeSubType() {
        return tradeSubType;
    }

    public void setTradeSubType(String tradeSubType) {
        this.tradeSubType = tradeSubType;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getChannelType() {
        return channelType;
    }

    @Override
    public String toString() {
        return "UnionpayTradeRefundRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", refundAmount='" + refundAmount + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", tradeSubType='" + tradeSubType + '\'' +
                ", bizType='" + bizType + '\'' +
                ", channelType='" + channelType + '\'' +
                '}';
    }
}
