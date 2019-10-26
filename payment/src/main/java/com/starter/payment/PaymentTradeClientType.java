package com.starter.payment;

import com.starter.payment.client.PaymentClientTypeEnum;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public interface PaymentTradeClientType {

    /**
     * 获取支付客户端
     *
     * @return
     */
    PaymentClientTypeEnum getClientType();
}
