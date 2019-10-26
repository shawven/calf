package com.starter.payment.client;

import com.starter.payment.PaymentTradeClientType;

/**
 * @author Shoven
 * @date 2019-10-09
 */
public interface WebQrcTradeClientType extends PaymentTradeClientType {

    @Override
    default PaymentClientTypeEnum getClientType() {
        return PaymentClientTypeEnum.WEB_QRC;
    }
}
