package com.test.payment.supplier.unionpay.sdk.request;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.test.payment.supplier.unionpay.sdk.UnionpayConstants.TIME_FORMAT;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradeRequest {


    /**
     * 系统时间，格式为YYYYMMDDhhmmss，必须取当前时间
     */
    private String requestTime = new SimpleDateFormat(TIME_FORMAT).format(new Date());

    /**
     * 交易币种（境内商户一般是156 人民币）
     */
    private String currencyCode = "156";

    /**
     * 接入类型，0：直连商户
     */
    private String accessType = "0";

    /**
     *  业务类型，B2C网关支付，手机wap支付
     */
    private String bizType = "000201";

    /**
     * 渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
     */
    private String channelType = "07";

    public UnionpayTradeRequest() {
    }

    public UnionpayTradeRequest(String bizType) {
        this.bizType = bizType;
    }

    public UnionpayTradeRequest(String bizType, String channelType) {
        this.bizType = bizType;
        this.channelType = channelType;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
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

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    @Override
    public String toString() {
        return "UnionpayTradeRequest{" +
                "requestTime='" + requestTime + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", accessType='" + accessType + '\'' +
                '}';
    }
}
