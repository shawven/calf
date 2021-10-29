package com.github.shawven.calf.payment.provider.unionpay;

import com.github.shawven.calf.payment.client.*;
import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayClient;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayConstants;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayException;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradePayRequest;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradeRefundRequest;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradeReversalRequest;
import com.github.shawven.calf.payment.provider.wechat.sdk.WXPayConstants;
import com.github.shawven.calf.payment.client.*;
import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.properties.UnionpayProperties;
import com.github.shawven.calf.payment.provider.PaymentProviderEnum;
import com.github.shawven.calf.payment.support.PaymentConstants;
import com.github.shawven.calf.payment.support.PaymentUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayB2CTemplate extends UnionpayTemplate {

    private UnionpayProperties properties;

    @Override
    public PaymentProviderEnum getProvider() {
        return PaymentProviderEnum.UNIONPAY;
    }

    @Override
    public String getBizType() {
        return UnionpayConstants.BIZ_TYPE_B2C;
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
        return UnionpayClientFacotry.getB2CInstance(getProperties());
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

    public static class App extends UnionpayB2CTemplate implements AppTradeClientType {

        @Override
        protected Map<String, String> doPay(UnionpayTradePayRequest request) throws UnionpayException {
            return getUnionpayClient().appExecute(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) {
            response.setSuccess(true);
            response.setPrepayId(rsp.get("tn"));
        }

        @Override
        protected UnionpayTradePayRequest getPayRequest(PaymentTradeRequest request) {
            UnionpayTradePayRequest payRequest = super.getPayRequest(request);
            payRequest.setTradeSubType("01");
            payRequest.setChannelType("08");
            return payRequest;
        }

        @Override
        protected UnionpayTradeRefundRequest getRefundRequest(PaymentTradeRefundRequest request) {
            UnionpayTradeRefundRequest refundRequest = super.getRefundRequest(request);
            refundRequest.setTradeSubType("00");
            refundRequest.setChannelType("08");
            return refundRequest;
        }
    }

    public static class Qrc extends UnionpayB2CTemplate implements QrcTradeClientType {

        @Override
        protected Map<String, String> doPay(UnionpayTradePayRequest request) throws UnionpayException {
            return getUnionpayClient().execute(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) {
            response.setSuccess(true);
            response.setCodeUrl(rsp.get("qrCode"));
        }

        @Override
        protected UnionpayTradePayRequest getPayRequest(PaymentTradeRequest request) {
            UnionpayTradePayRequest payRequest = super.getPayRequest(request);
            payRequest.setTradeSubType("07");
            payRequest.setChannelType("08");
            return payRequest;
        }

        @Override
        protected UnionpayTradeRefundRequest getRefundRequest(PaymentTradeRefundRequest request) {
            UnionpayTradeRefundRequest refundRequest = super.getRefundRequest(request);
            refundRequest.setTradeSubType("00");
            refundRequest.setChannelType("08");
            return refundRequest;
        }

        @Override
        public String getBizType() {
            return UnionpayConstants.BIZ_TYPE_F2F;
        }
    }

    public static class F2f extends UnionpayB2CTemplate implements F2fTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            UnionpayTradePayRequest unionpayRequest = getPayRequest(request);
            PaymentTradeResponse response = new PaymentTradeResponse();
            boolean needQuery = false;

            try {
                if (logger.isInfoEnabled()) {
                    logger.info(request, "支付请求参数：{}", unionpayRequest);
                }

                Map<String, String> rsp = getUnionpayClient().execute(unionpayRequest);
                if (logger.isInfoEnabled()) {
                    logger.info(request, "支付响应参数：{}", getPrettyMapForPrinter(rsp));
                }

                String respCode = rsp.get("respCode");
                if (UnionpayConstants.SUCCESS_CODE.equals(respCode)) {
                    response.setSuccess(true);
                    response.setTradeNo(rsp.get("queryId"));
                } else if ("01".equals(respCode) || "03".equals(respCode)
                        || "05".equals(respCode) || "12".equals(respCode)) {
                    needQuery = true;
                    response.setErrorMsg(rsp.get("respMsg"));
                } else {
                    response.setErrorMsg("支付失败：" + rsp.get("respMsg"));
                }
            } catch (UnionpayException e) {
                if (e.getCause() instanceof IOException) {
                    needQuery = true;
                }
                response.setErrorMsg("支付错误：" + e.getMessage());
            }
            if (!response.isSuccess()) {
                if (needQuery) {
                    if (logger.isInfoEnabled()) {
                        logger.info(request, "支付未完成[{}]，正在查询订单", response.getErrorMsg());
                    }

                    queryTradeIfFailed(request, response);
                } else {
                    if (logger.isErrorEnabled()) {
                        logger.error(request, response.getErrorMsg());
                    }
                }
            }
            return response;
        }

        private void queryTradeIfFailed(PaymentTradeRequest request, PaymentTradeResponse response) {
            AtomicReference<PaymentTradeQueryResponse> reference = new AtomicReference<>(new PaymentTradeQueryResponse());
            PaymentUtils.runningUntilSuccess(new PaymentUtils.ScheduledTask() {
                @Override
                public void run() {
                    PaymentTradeQueryRequest queryRequest = new PaymentTradeQueryRequest(request);
                    queryRequest.setOutTradeNo(request.getOutTradeNo());
                    PaymentTradeQueryResponse result;
                    try {
                        result = query(queryRequest);
                    } catch (Exception e) {
                        return;
                    }
                    // 成功或者不存在的交易直接取消循环
                    if (result.isSuccess() || result.isNotExist()) {
                        this.cancel();
                    }
                    reference.set(result);
                }
            }, 3, 30).await();
            PaymentTradeQueryResponse queryResponse = reference.get();

            if (queryResponse.isSuccess()) {
                response.setSuccess(true);
                response.setTradeNo(queryResponse.getTradeNo());
                response.setOutTradeNo(queryResponse.getOutTradeNo());
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info(request, "支付未完成正在取消订单");
                }

                if (cancel(request)) {
                    response.setErrorMsg("支付未完成已取消订单");
                    if (logger.isInfoEnabled()) {
                        logger.info(request, response.getErrorMsg());
                    }

                } else {
                    response.setErrorMsg("支付未完成且取消订单异常");
                    if (logger.isErrorEnabled()) {
                        logger.error(request, response.getErrorMsg());
                    }
                }
            }
        }

        public boolean cancel(PaymentTradeRequest request) {
            return PaymentUtils.runningUntilSuccess(() -> onceCancel(request),
                    i -> {
                        if (logger.isInfoEnabled()) {
                            logger.info(request, "正在第" + i + "次重试取消订单");
                        }
                    }, 3);

        }

        /**
         * @param request
         * @return 成功与否
         */
        public boolean onceCancel(PaymentTradeRequest request) {
            UnionpayTradeReversalRequest reversalRequest = getReversalRequest(request);
            try {
                if (logger.isInfoEnabled()) {
                    logger.info(request, "取消订单请求参数：{}", reversalRequest);
                }

                Map<String, String> rsp = getUnionpayClient().reversal(reversalRequest);
                if (logger.isInfoEnabled()) {
                    logger.info(request, "取消订单响应参数：{}", getPrettyMapForPrinter(rsp));
                }

                String returnCode = rsp.get("return_code");
                if (WXPayConstants.SUCCESS.equals(returnCode)) {
                    String resultCode = rsp.get("result_code");
                    if (!WXPayConstants.SUCCESS.equals(resultCode)) {
                        if (logger.isInfoEnabled()) {
                            logger.info(request, "取消订单失败：{}", rsp.get("err_code_des"));
                        }

                        return false;
                    }
                    return true;
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info(request, "取消订单失败：{}", rsp.get("return_msg"));
                    }

                    return false;
                }
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(request, "取消订单错误：{}", e.getMessage());
                }
                return false;
            }
        }

        @Override
        protected UnionpayTradePayRequest getPayRequest(PaymentTradeRequest request) {
            String authCode = request.get(PaymentConstants.AUTH_CODE);
            if (PaymentUtils.isBlankString(authCode)) {
                throw new IllegalArgumentException("支付授权码不能为空");
            }
            String storeId = request.get(PaymentConstants.STORE_ID);
            if (PaymentUtils.isBlankString(storeId)) {
                throw new IllegalArgumentException("门店编号不能为空");
            }
            UnionpayTradePayRequest payRequest = super.getPayRequest(request);
            payRequest.setTradeSubType("06");
            payRequest.setChannelType("08");
            payRequest.setAuthCode(authCode);
            payRequest.setTermId(storeId);
            return payRequest;
        }

        @Override
        protected UnionpayTradeRefundRequest getRefundRequest(PaymentTradeRefundRequest request) {
            UnionpayTradeRefundRequest refundRequest = super.getRefundRequest(request);
            refundRequest.setTradeSubType("00");
            refundRequest.setChannelType("08");
            return refundRequest;
        }

        private UnionpayTradeReversalRequest getReversalRequest(PaymentTradeRequest request) {
            UnionpayTradeReversalRequest reversalRequest = new UnionpayTradeReversalRequest();
            reversalRequest.setBizType(getBizType());
            reversalRequest.setTradeType("99");
            reversalRequest.setTradeSubType("01");
            reversalRequest.setChannelType("08");
            reversalRequest.setOutTradeNo(request.getOutTradeNo());
            return reversalRequest;
        }

        @Override
        public String getBizType() {
            return UnionpayConstants.BIZ_TYPE_F2F;
        }
    }

}

