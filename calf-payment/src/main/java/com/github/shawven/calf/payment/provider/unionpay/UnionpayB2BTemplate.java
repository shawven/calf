package com.github.shawven.calf.payment.provider.unionpay;

import com.github.shawven.calf.payment.client.WapTradeClientType;
import com.github.shawven.calf.payment.client.WebTradeClientType;
import com.github.shawven.calf.payment.domain.PaymentTradeRefundRequest;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayClient;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayConstants;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayException;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradePayRequest;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradeRefundRequest;
import com.github.shawven.calf.payment.properties.UnionpayProperties;
import com.github.shawven.calf.payment.provider.PaymentProviderEnum;
import com.github.shawven.calf.payment.domain.PaymentTradeRequest;
import com.github.shawven.calf.payment.domain.PaymentTradeResponse;

import java.util.Map;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayB2BTemplate extends UnionpayTemplate {

    private UnionpayProperties properties;

    @Override
    public PaymentProviderEnum getProvider() {
        return PaymentProviderEnum.UNIONPAY_B2B;
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

    private abstract static class PagePaySupport extends UnionpayB2CTemplate {

        @Override
        protected Map<String, String> doPay(UnionpayTradePayRequest request) throws UnionpayException {
            return getUnionpayClient().pageExecute(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) {
            response.setSuccess(true);
            response.setForm(rsp.get("form"));
        }

        @Override
        protected UnionpayTradePayRequest getPayRequest(PaymentTradeRequest request) {
            UnionpayTradePayRequest payRequest = super.getPayRequest(request);
            payRequest.setReturnUrl(getProperties().getReturnUrl());
            payRequest.setTradeSubType("01");
            payRequest.setChannelType(getChannelType());
            return payRequest;
        }

        @Override
        protected UnionpayTradeRefundRequest getRefundRequest(PaymentTradeRefundRequest request) {
            UnionpayTradeRefundRequest refundRequest = super.getRefundRequest(request);
            refundRequest.setTradeSubType("00");
            refundRequest.setChannelType(getChannelType());
            return refundRequest;
        }

        protected abstract String getChannelType();
    }

    public static class Web extends PagePaySupport implements WebTradeClientType {

        @Override
        protected String getChannelType() {
            return "07";
        }
    }

    public static class Wap extends PagePaySupport implements WapTradeClientType {

        @Override
        protected String getChannelType() {
            return "08";
        }
    }
}

