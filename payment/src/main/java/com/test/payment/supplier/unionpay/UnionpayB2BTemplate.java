package com.test.payment.supplier.unionpay;

import com.test.payment.client.WapTradeClientType;
import com.test.payment.client.WebTradeClientType;
import com.test.payment.domain.PaymentTradeRequest;
import com.test.payment.domain.PaymentTradeResponse;
import com.test.payment.properties.UnionpayProperties;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.unionpay.sdk.UnionpayClient;
import com.test.payment.supplier.unionpay.sdk.UnionpayConstants;
import com.test.payment.supplier.unionpay.sdk.UnionpayException;
import com.test.payment.supplier.unionpay.sdk.request.UnionpayTradePayRequest;

import java.util.Map;

import static com.test.payment.supplier.PaymentSupplierEnum.UNIONPAY_B2B;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayB2BTemplate extends UnionpayTemplate {

    private UnionpayProperties properties;

    @Override
    public PaymentSupplierEnum getSupplier() {
        return UNIONPAY_B2B;
    }

    @Override
    public String getBizType() {
        return UnionpayConstants.BIZ_TYPE_B2B;
    }

    @Override
    public void setProperties(UnionpayProperties properties) {
        this.properties = properties;
    }

    @Override
    public UnionpayProperties getProperties() {
        return properties;
    }

    @Override
    public UnionpayClient getUnionpayClient() {
        return UnionpayClientFacotry.getB2BInstance(getProperties());
    }

    public static class Web extends UnionpayB2BTemplate implements WebTradeClientType {

        @Override
        protected Map<String, String> doPay(UnionpayTradePayRequest request) throws UnionpayException {
            return getUnionpayClient().pagePay(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp)  {
            response.setSuccess(true);
            response.setForm(rsp.get("form"));
        }

        @Override
        protected UnionpayTradePayRequest getPayRequest(PaymentTradeRequest request) {
            UnionpayTradePayRequest payRequest = super.getPayRequest(request);
            payRequest.setTradeType("01");
            payRequest.setTradeSubType("01");
            payRequest.setChannelType("07");
            return payRequest;
        }
    }

    public static class Wap extends UnionpayB2BTemplate implements WapTradeClientType {

        @Override
        protected Map<String, String> doPay(UnionpayTradePayRequest request) throws UnionpayException {
            return getUnionpayClient().pagePay(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp)  {
            response.setSuccess(true);
            response.setForm(rsp.get("form"));
        }

        @Override
        protected UnionpayTradePayRequest getPayRequest(PaymentTradeRequest request) {
            UnionpayTradePayRequest payRequest = super.getPayRequest(request);
            payRequest.setTradeType("01");
            payRequest.setTradeSubType("01");
            payRequest.setChannelType("07");
            return payRequest;
        }
    }
}

