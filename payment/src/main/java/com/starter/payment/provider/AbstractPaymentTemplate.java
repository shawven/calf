package com.starter.payment.provider;

import com.starter.payment.PaymentOperations;
import com.starter.payment.support.PaymentContextHolder;
import com.starter.payment.properties.AppProperties;
import com.starter.payment.support.PaymentLogger;

/**
 * @author Shoven
 * @date 2019-09-20
 */
public abstract class AbstractPaymentTemplate implements PaymentOperations {

    protected PaymentLogger logger = PaymentLogger.getLogger(getClass());

    public AppProperties getAppProperties() {
        return PaymentContextHolder.getAppProperties();
    }
}

