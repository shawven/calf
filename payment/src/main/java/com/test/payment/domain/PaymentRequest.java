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
        Objects.requireNonNull(paymentSupplier, "支付供应商不能为空");
        String supplierName = paymentSupplier.toUpperCase();
        this.paymentSupplier = PaymentSupplierEnum.valueOf(supplierName);
        this.paymentClientType = null;
        Objects.requireNonNull(this.paymentSupplier, () -> "不存在在此供应商" + supplierName);
    }

    public PaymentRequest(PaymentSupplierEnum paymentSupplier) {
        Objects.requireNonNull(paymentSupplier, "支付供应商不能为空");
        this.paymentSupplier = paymentSupplier;
        this.paymentClientType = null;
    }

    public PaymentRequest(String paymentSupplier, String paymentClientType) {
        Objects.requireNonNull(paymentSupplier, "支付供应商不能为空");
        Objects.requireNonNull(paymentClientType, "客户端类型不能为空");
        String supplierName = paymentSupplier.toUpperCase();
        String clientTypeName = paymentClientType.toUpperCase();
        this.paymentSupplier = PaymentSupplierEnum.valueOf(supplierName);
        this.paymentClientType = PaymentClientTypeEnum.valueOf(clientTypeName);
        Objects.requireNonNull(this.paymentSupplier, () -> "不存在在此供应商" + supplierName);
        Objects.requireNonNull(this.paymentClientType, () -> supplierName + "不支持" + clientTypeName + "支付方式");
    }

    public PaymentRequest(PaymentSupplierEnum paymentSupplier, PaymentClientTypeEnum paymentClientType) {
        Objects.requireNonNull(paymentSupplier, "支付供应商不能为空");
        Objects.requireNonNull(paymentClientType, "客户端类型不能为空");
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

    public void setPrincipal(String principal) {
        this.principal = principal;
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
