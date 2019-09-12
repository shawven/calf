package com.test.payment;

import com.test.payment.supplier.PaymentSupplierEnum;

/**
 * @author Shoven
 * @date 2019-09-05
 */
public interface PaymentWayType extends PaymentTradeClientType {

    /**
     * 获取支付渠道
     *
     * @return
     */
    PaymentSupplierEnum getSupplier();
}
