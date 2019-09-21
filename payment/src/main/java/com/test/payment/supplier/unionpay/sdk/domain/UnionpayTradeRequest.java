package com.test.payment.supplier.unionpay.sdk.domain;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class UnionpayTradeRequest {

    private static String timeFormat = "yyyyMMddHHmmss";

    /**
     * 系统时间，格式为YYYYMMDDhhmmss，必须取当前时间
     */
    private String requestTime = new SimpleDateFormat(timeFormat).format(new Date());

    /**
     * 交易币种（境内商户一般是156 人民币）
     */
    private String currencyCode = "156";


    /**
     * 接入类型，0：直连商户
     */
    private String accessType = "0";

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

    @Override
    public String toString() {
        return "UnionpayTradeRequest{" +
                "requestTime='" + requestTime + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", accessType='" + accessType + '\'' +
                '}';
    }
}
