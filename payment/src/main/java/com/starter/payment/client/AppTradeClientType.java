package com.starter.payment.client;

import com.starter.payment.PaymentTradeClientType;

/**
 * @author Shoven
 * @date 2019-10-08
 */
public interface AppTradeClientType extends PaymentTradeClientType {
    @Override
    default PaymentClientTypeEnum getClientType() {
        return PaymentClientTypeEnum.APP;
    }
}
