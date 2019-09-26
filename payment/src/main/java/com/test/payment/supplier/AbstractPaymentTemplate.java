package com.test.payment.supplier;

import com.test.payment.PaymentOperations;
import com.test.payment.properties.GlobalProperties;
import com.test.payment.support.PaymentContextHolder;
import com.test.payment.support.PaymentLogger;

/**
 * @author Shoven
 * @date 2019-09-20
 */
public abstract class AbstractPaymentTemplate implements PaymentOperations {

    private GlobalProperties globalProperties;

    protected PaymentLogger logger = PaymentLogger.getLogger(getClass());

    public GlobalProperties getGlobalProperties() {
        return PaymentContextHolder.getGlobalProperties();
    }
}

