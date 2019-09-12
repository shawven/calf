package com.test.payment.client;

import com.test.payment.PaymentTradeClientType;

/**
 * @author Shoven
 * @date 2019-09-03
 */
public interface WapTradeClientType extends PaymentTradeClientType {
    @Override
    default PaymentClientTypeEnum getClientType() {
        return PaymentClientTypeEnum.WAP;
    }
}
