package com.github.shawven.calf.payment.provider;


/**
 * @author Shoven
 * @date 2019-08-27
 */
public enum PaymentProviderEnum {

    /**
     * 支付宝
     */
    ALIPAY("支付宝"),

    /**
     * 微信
     */
    WECHAT("微信"),

    /**
     * 银联
     */
    UNIONPAY("银联"),

    /**
     * 银联B2B
     */
    UNIONPAY_B2B("银联(公账)");

    private String name;

    PaymentProviderEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
