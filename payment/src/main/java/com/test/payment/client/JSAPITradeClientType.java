package com.test.payment.client;

import com.test.payment.PaymentTradeClientType;

/**
 * @author Shoven
 * @date 2019-10-08
 */
public interface JSAPITradeClientType extends PaymentTradeClientType {
    @Override
    default PaymentClientTypeEnum getClientType() {
        return PaymentClientTypeEnum.JSAPI;
    }
}
