package com.test.payment.domain;

import com.test.payment.client.PaymentClientTypeEnum;
import com.test.payment.supplier.PaymentSupplierEnum;

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
     * 平台供应商
     */
    private PaymentSupplierEnum paymentSupplier;

    /**
     * 客户端类型
     */
    private PaymentClientTypeEnum paymentClientType;


    public PaymentRequest(PaymentRequest request) {
        this.principal = request.getPrincipal();
        this.paymentSupplier = request.getPaymentSupplier();
        this.paymentClientType = request.getPaymentClientType();
    }

    public PaymentRequest(String paymentSupplier) {
        this.paymentSupplier = PaymentSupplierEnum.valueOf(paymentSupplier.toUpperCase());
        this.paymentClientType = null;
    }

    public PaymentRequest(PaymentSupplierEnum paymentSupplier) {
        this.paymentSupplier = paymentSupplier;
        this.paymentClientType = null;
    }

    public PaymentRequest(String paymentSupplier, String paymentClientType) {
        Objects.requireNonNull(paymentSupplier, "支付供应商不能为空");
        Objects.requireNonNull(paymentClientType, "客户端类型不能为空");
        this.paymentSupplier = PaymentSupplierEnum.valueOf(paymentSupplier.toUpperCase());
        this.paymentClientType = PaymentClientTypeEnum.valueOf(paymentClientType.toUpperCase());
    }

    public PaymentRequest(PaymentSupplierEnum paymentSupplier, PaymentClientTypeEnum paymentClientType) {
        this.paymentSupplier = paymentSupplier;
        this.paymentClientType = paymentClientType;
    }

    public PaymentSupplierEnum getPaymentSupplier() {
        return paymentSupplier;
    }

    public PaymentClientTypeEnum getPaymentClientType() {
        return paymentClientType;
    }

    public String getPrincipal() {
        return principal;
    }

    public PaymentRequest setPrincipal(String principal) {
        this.principal = principal;
        return this;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "principal='" + principal + '\'' +
                ", paymentSupplier=" + paymentSupplier +
                ", paymentClientType=" + paymentClientType +
                '}';
    }
}
