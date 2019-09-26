package com.test.payment;

import com.test.payment.domain.*;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public interface PaymentOperations extends PaymentWayType {

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
    default PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        throw new UnsupportedOperationException();
    }

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
    default PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        throw new UnsupportedOperationException();
    }

    /**
     * 退款异步通知
     *
     * @return
     */
    default PaymentTradeCallbackResponse refundNotify(PaymentTradeCallbackRequest request) {
        throw new UnsupportedOperationException();
    }

}
