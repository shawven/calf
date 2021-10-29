package com.github.shawven.calf.payment.domain;

import com.github.shawven.calf.payment.provider.PaymentProviderEnum;
import com.github.shawven.calf.payment.client.PaymentClientTypeEnum;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public class PaymentRequest implements Serializable {

    /**
     * 当事人（用户）、有意义的唯一ID
     */
    private String principal;

    /**
     * 平台提供商
     */
    private PaymentProviderEnum paymentProvider;

    /**
     * 客户端类型
     */
    private PaymentClientTypeEnum paymentClientType;

    public PaymentRequest(PaymentRequest request) {
        this.principal = request.getPrincipal();
        this.paymentProvider = request.getPaymentProvider();
        this.paymentClientType = request.getPaymentClientType();
    }

    public PaymentRequest(String paymentProvider) {
        Objects.requireNonNull(paymentProvider, "支付提供商不能为空");
        String providerName = paymentProvider.toUpperCase();
        this.paymentProvider = PaymentProviderEnum.valueOf(providerName);
        this.paymentClientType = null;
        Objects.requireNonNull(this.paymentProvider, () -> "不存在在此提供商" + providerName);
    }

    public PaymentRequest(PaymentProviderEnum paymentProvider) {
        Objects.requireNonNull(paymentProvider, "支付提供商不能为空");
        this.paymentProvider = paymentProvider;
        this.paymentClientType = null;
    }

    public PaymentRequest(String paymentProvider, String paymentClientType) {
        Objects.requireNonNull(paymentProvider, "支付提供商不能为空");
        Objects.requireNonNull(paymentClientType, "客户端类型不能为空");
        String providerName = paymentProvider.toUpperCase();
        String clientTypeName = paymentClientType.toUpperCase();
        this.paymentProvider = PaymentProviderEnum.valueOf(providerName);
        this.paymentClientType = PaymentClientTypeEnum.valueOf(clientTypeName);
        Objects.requireNonNull(this.paymentProvider, () -> "不存在在此提供商" + providerName);
        Objects.requireNonNull(this.paymentClientType, () -> providerName + "不支持" + clientTypeName + "支付方式");
    }

    public PaymentRequest(PaymentProviderEnum paymentProvider, PaymentClientTypeEnum paymentClientType) {
        Objects.requireNonNull(paymentProvider, "支付提供商不能为空");
        Objects.requireNonNull(paymentClientType, "客户端类型不能为空");
        this.paymentProvider = paymentProvider;
        this.paymentClientType = paymentClientType;
    }

    public PaymentProviderEnum getPaymentProvider() {
        return paymentProvider;
    }

    public PaymentClientTypeEnum getPaymentClientType() {
        return paymentClientType;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "principal='" + principal + '\'' +
                ", paymentProvider=" + paymentProvider +
                ", paymentClientType=" + paymentClientType +
                '}';
    }
}
