package com.github.shawven.calf.payment.provider.unionpay.sdk.request;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayConstants.TIME_FORMAT;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public abstract class UnionpayTradeRequest {

    /**
     * 系统时间，格式为YYYYMMDDhhmmss，必须取当前时间
     */
    private String requestTime = new SimpleDateFormat(TIME_FORMAT).format(new Date());

    /**
     * 交易币种（境内商户一般是156 人民币）
     */
    protected String currencyCode = "156";

    /**
     * 接入类型，0：直连商户
     */
    protected String accessType = "0";

    /**
     * 业务类型，B2C、B2B、二维码、APP
     */
    protected String bizType;

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

    public abstract String getTradeType();

    public abstract void setTradeType(String bizType);

    public abstract String getTradeSubType();

    public abstract void setTradeSubType(String bizType);

    @Override
    public String toString() {
        return "UnionpayTradeRequest{" +
                "requestTime='" + requestTime + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", accessType='" + accessType + '\'' +
                ", bizType='" + bizType + '\'' +
                '}';
    }
}
