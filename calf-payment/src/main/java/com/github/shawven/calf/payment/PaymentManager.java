package com.github.shawven.calf.payment;

import com.github.shawven.calf.payment.client.PaymentClientTypeEnum;
import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.provider.PaymentProviderEnum;
import com.github.shawven.calf.payment.domain.*;

import java.util.Set;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public interface PaymentManager {

    /**
     * 根据支付方式列举可以的支付提供商
     *
     * @param paymentClient
     * @return
     */
    Set<PaymentProviderEnum> listAvailableProviders(PaymentClientTypeEnum paymentClient);

    /**
     * 支付
     *
     * @return
     */
    PaymentTradeResponse pay(PaymentTradeRequest request);

    /**
     * 查询支付结果
     *
     * @return
     */
    PaymentTradeQueryResponse query(PaymentTradeQueryRequest request);

    /**
     * 同步跳转
     *
     * @return
     */
    PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request);

    /**
     * 异步通知
     *
     * @return
     */
    PaymentTradeCallbackResponse asyncNotify(PaymentTradeCallbackRequest request);

    /**
     * 退款
     *
     * @return
     */
    PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request);

    /**
     * 查询退款状态
     *
     * @return
     */
    PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request);

}
