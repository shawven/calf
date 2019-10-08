package com.test.payment.client;

/**
 * 支付客户端类型
 *
 * @author Shoven
 * @date 2019-08-27
 */
public enum PaymentClientTypeEnum {

    /**
     * 网站
     */
    WEB("WEB网站"),

    /**
     * 手机WAP
     */
    WAP("WAP网站"),

    /**
     * APP客户端
     */
    APP("APP客户端"),

    /**
     * 二维码
     */
    QRC("二维码"),

    /**
     * 微信支付宝客户端等内置浏览器
     */
    JSAPI("JSAPI"),

    /**
     * 当面付
     */
    F2F("当面付"),

    /**
     * 统一终端
     */
    UNIFIED("统一终端");

    private String name;

    PaymentClientTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
