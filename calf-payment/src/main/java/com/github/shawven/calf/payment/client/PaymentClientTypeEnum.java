package com.github.shawven.calf.payment.client;

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
     * 网站
     */
    WEB_QRC("WEB网站扫码"),

    /**
     * 手机WAP
     */
    WAP("WAP网站"),

    /**
     * APP客户端
     */
    APP("APP客户端"),

    /**
     * 微信支付宝客户端等内置浏览器
     */
    JSAPI("JSAPI"),
    /**
     * 二维码 （主扫）当面付扫商家生成的二维码
     */
    QRC("二维码"),

    /**
     * 付款码 （被扫）当面付出示付款码给商家
     */
    F2F("付款码"),

    NONE("");

    private String name;

    PaymentClientTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
