package com.test.payment.supplier.unionpay.sdk.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradeQueryRequest extends UnionpayTradeRequest {

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 查询交易号
     */
    private String tradeNo;

    /**
     * 交易类型
     */
    private String tradeType = "00";

    /**
     * 交易子类型
     */
    private String tradeSubType = "00";

    /**
     *  业务类型，B2C网关支付，手机wap支付
     */
    private String bizType = "000201";

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

    @Override
    public String toString() {
        return "UnionpayTradeQueryRequest{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", tradeSubType='" + tradeSubType + '\'' +
                ", bizType='" + bizType + '\'' +
                '}';
    }
}
