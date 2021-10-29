package com.github.shawven.calf.payment.support;

import com.github.shawven.calf.payment.properties.AppProperties;

/**
 * @author Shoven
 * @date 2019-09-23
 */
public class PaymentContextHolder {

    private static AppProperties appProperties;

    private static HttpUtil http;

    public static AppProperties getAppProperties() {
        return appProperties;
    }

    public static void setAppProperties(AppProperties appProperties) {
        PaymentContextHolder.appProperties = appProperties;
    }

    public static HttpUtil getHttp() {
        return http;
    }

    public static void setHttp(HttpUtil httpUtil) {
        http = httpUtil;
    }
}
