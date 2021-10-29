package com.github.shawven.calf.payment.provider.alipay.sdk;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class AlipayConstants {

    public static final String GATEWAY_URL = "https://openapi.alipay.com/gateway.do";
    public static final String SANDBOX_GATEWAY_URL = "https://openapi.alipaydev.com/gateway.do";

    public static final String WEB_PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";
    public static final String WAP_PRODUCT_CODE = "QUICK_WAP_WAY";
    public static final String APP_PRODUCT_CODE = "QUICK_MSECURITY_PAY";
    public static final String F2F_PRODUCT_CODE = "FACE_TO_FACE_PAYMENT";

    public static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    public static final String TRADE_FINISHED = "TRADE_FINISHED";
    public static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

    public static final String REPLAY_SUCCESS = "success";

    public static final String QUERY_TRADE_NOT_EXIST = "ACQ.TRADE_NOT_EXIST";
}
