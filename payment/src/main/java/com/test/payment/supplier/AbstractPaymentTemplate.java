package com.test.payment.supplier;

import com.test.payment.PaymentOperations;
import com.test.payment.client.PaymentClientTypeEnum;
import com.test.payment.domain.*;
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

    @Override
    public PaymentTradeResponse pay(PaymentTradeRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaymentTradeCallbackResponse asyncNotify(PaymentTradeCallbackRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaymentTradeCallbackResponse refundNotify(PaymentTradeCallbackRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        throw new UnsupportedOperationException();
    }
}
