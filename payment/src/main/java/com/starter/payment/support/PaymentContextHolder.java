package com.starter.payment.support;

import com.starter.payment.properties.GlobalProperties;

/**
 * @author Shoven
 * @date 2019-09-23
 */
public class PaymentContextHolder {

    private static GlobalProperties globalProperties;

    private static HttpUtil http;

    public static GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    public static void setGlobalProperties(GlobalProperties globalProperties) {
        PaymentContextHolder.globalProperties = globalProperties;
    }

    public static HttpUtil getHttp() {
        return http;
    }

    public static void setHttp(HttpUtil httpUtil) {
        http = httpUtil;
    }
}
