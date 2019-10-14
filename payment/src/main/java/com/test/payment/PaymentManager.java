package com.test.payment;

import com.test.payment.client.PaymentClientTypeEnum;
import com.test.payment.domain.*;
import com.test.payment.supplier.PaymentSupplierEnum;

import java.util.Set;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public interface PaymentManager {

    /**
     * 根据支付方式列举可以的支付供应商
     *
     * @param paymentClient
     * @return
     */
    Set<PaymentSupplierEnum> listAvailableSuppliers(PaymentClientTypeEnum paymentClient);

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
