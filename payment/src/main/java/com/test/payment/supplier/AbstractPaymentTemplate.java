package com.test.payment.supplier;

import com.test.payment.PaymentOperations;
import com.test.payment.properties.PaymentProperties;
import com.test.payment.support.CurrencyTools;
import com.test.payment.support.PaymentLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Shoven
 * @date 2019-09-20
 */
public abstract class AbstractPaymentTemplate implements PaymentOperations {

    private PaymentProperties paymentProperties;

    protected PaymentLogger logger = PaymentLogger.getLogger(getClass());

    public PaymentProperties getPaymentProperties() {
        return paymentProperties;
    }

    public void setPaymentProperties(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
    }


}
