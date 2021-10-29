package com.github.shawven.calf.payment.client;

import com.github.shawven.calf.payment.PaymentTradeClientType;

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
