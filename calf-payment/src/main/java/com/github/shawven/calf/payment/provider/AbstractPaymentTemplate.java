package com.github.shawven.calf.payment.provider;

import com.github.shawven.calf.payment.PaymentOperations;
import com.github.shawven.calf.payment.properties.AppProperties;
import com.github.shawven.calf.payment.support.PaymentContextHolder;
import com.github.shawven.calf.payment.support.PaymentLogger;

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

