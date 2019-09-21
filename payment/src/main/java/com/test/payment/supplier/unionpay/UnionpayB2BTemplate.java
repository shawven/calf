package com.test.payment.supplier.unionpay;

import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.unionpay.sdk.UnionpayConstants;

import static com.test.payment.supplier.PaymentSupplierEnum.UNIONPAY_B2B;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayB2BTemplate extends UnionpayTemplate {

    @Override
    public PaymentSupplierEnum getSupplier() {
        return UNIONPAY_B2B;
    }

    @Override
    public String getBizType() {
        return UnionpayConstants.B2B;
    }
}

