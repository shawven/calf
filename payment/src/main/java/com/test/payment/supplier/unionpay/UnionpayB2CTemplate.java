package com.test.payment.supplier.unionpay;

import com.test.payment.client.WapTradeClientType;
import com.test.payment.client.WebTradeClientType;
import com.test.payment.properties.UnionpayB2CProperties;
import com.test.payment.properties.UnionpayProperties;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.unionpay.sdk.UnionpayClient;
import com.test.payment.supplier.unionpay.sdk.UnionpayConstants;

import static com.test.payment.supplier.PaymentSupplierEnum.UNIONPAY;
import static com.test.payment.supplier.PaymentSupplierEnum.UNIONPAY_B2B;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayB2CTemplate extends UnionpayTemplate {

    private UnionpayProperties properties;

    @Override
    public PaymentSupplierEnum getSupplier() {
        return UNIONPAY;
    }

    @Override
    public String getBizType() {
        return UnionpayConstants.B2C;
    }

    @Override
    public void setProperties(UnionpayProperties properties) {
        this.properties = properties;
    }

    @Override
    public UnionpayProperties getProperties() {
        return properties;
    }

    @Override
    public UnionpayClient getUnionpayClient() {
        return UnionpayClientFacotry.getB2CInstance(getProperties());
    }

    public static class Web extends UnionpayB2CTemplate implements WebTradeClientType {

        @Override
        public String getChannelType() {
            return UnionpayConstants.PC;
        }
    }

    public static class Wap extends UnionpayB2CTemplate implements WapTradeClientType {

        @Override
        public String getChannelType() {
            return UnionpayConstants.WAP;
        }
    }
}

