package com.test.payment.support;

import com.test.payment.properties.PaymentProperties;

/**
 * @author Shoven
 * @date 2019-09-23
 */
public class PaymentContextHolder {

    private static PaymentProperties globalProperties;

    private static HttpUtil http;


    public static PaymentProperties getGlobalProperties() {
        return globalProperties;
    }

    public static void setGlobalProperties(PaymentProperties globalProperties) {
        PaymentContextHolder.globalProperties = globalProperties;
    }

    public static HttpUtil getHttp() {
        return http;
    }

    public static void setHttp(HttpUtil httpUtil) {
        http = httpUtil;
    }
}
