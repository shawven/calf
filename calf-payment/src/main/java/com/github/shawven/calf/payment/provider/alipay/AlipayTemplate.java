package com.github.shawven.calf.payment.provider.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.github.shawven.calf.payment.client.*;
import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.provider.alipay.sdk.AlipayConstants;
import com.github.shawven.calf.payment.client.*;
import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.properties.AlipayProperties;
import com.github.shawven.calf.payment.provider.AbstractPaymentTemplate;
import com.github.shawven.calf.payment.provider.PaymentProviderEnum;
import com.github.shawven.calf.payment.support.CurrencyTools;
import com.github.shawven.calf.payment.support.PaymentConstants;
import com.github.shawven.calf.payment.support.PaymentUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.shawven.calf.payment.provider.PaymentProviderEnum.ALIPAY;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class AlipayTemplate extends AbstractPaymentTemplate {

    protected AlipayProperties properties;

    @Override
    public PaymentProviderEnum getProvider() {
        return ALIPAY;
    }

    @Override
    public PaymentTradeResponse pay(PaymentTradeRequest request) {
        PaymentTradeResponse response = new PaymentTradeResponse();
        AlipayRequest<? extends AlipayResponse> payRequest = getPayRequest(request);
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "预支付请求参数：{}", PaymentUtils.toString(payRequest));
            }

            //网页支付
            AlipayResponse rsp = doPay(payRequest);
            if (logger.isInfoEnabled()) {
                logger.info(request, "预支付响应参数：{}", PaymentUtils.toString(rsp));
            }

            if (rsp.isSuccess()) {
                setPaySuccessResponse(response, rsp);
            } else {
                response.setErrorMsg(rsp.getSubMsg());
                if (logger.isErrorEnabled()) {
                    logger.error(request, "预支付失败：{}", response.getErrorMsg());
                }
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("预支付错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        PaymentTradeCallbackResponse response = new PaymentTradeCallbackResponse();
        try {
            Map<String, String> params = request.getParams();
            if (logger.isInfoEnabled()) {
                logger.info(request, "同步回调接受参数：{}", params);
            }

            boolean validation = AlipaySignature.rsaCheckV1(params, properties.getPublicKey(),
                    properties.getCharset(), properties.getSignType());
            if (validation) {
                String tradeNo = params.get("trade_no");
                // 新版不支持交易状态 转用查询
                if (logger.isInfoEnabled()) {
                    logger.info(request, "同步回调转查询交易：{}", params);
                }

                PaymentTradeQueryRequest queryRequest = new PaymentTradeQueryRequest(request);
                queryRequest.setTradeNo(tradeNo);
                PaymentTradeQueryResponse queryResponse = query(queryRequest);

                response.setSuccess(queryResponse.isSuccess());
                response.setAmount(CurrencyTools.ofYuan(queryResponse.getAmount()));
                response.setTradeNo(queryResponse.getTradeNo());
                response.setOutTradeNo(queryResponse.getOutTradeNo());
            } else {
                response.setErrorMsg("同步回调验签失败");
                if (logger.isErrorEnabled()) {
                    logger.error(request, response.getErrorMsg());
                }
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("同步跳转错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse asyncNotify(PaymentTradeCallbackRequest request) {
        PaymentTradeCallbackResponse response = new PaymentTradeCallbackResponse();
        try {
            Map<String, String> params = request.getParams();
            logger.info(request, "异步回调接受参数：{}", params);
            boolean validation = AlipaySignature.rsaCheckV1(params, properties.getPublicKey(),
                    properties.getCharset(), properties.getSignType());
            if (validation) {
                String tradeNo = params.get("trade_no");
                String outTradeNo = params.get("out_trade_no");
                String tradeStatus = params.get("trade_status");
                String totalAmount = params.get("total_amount");
                String tradeStatusDesc;
                if (AlipayConstants.TRADE_SUCCESS.equals(tradeStatus)
                        || AlipayConstants.TRADE_FINISHED.equals(tradeStatus)) {
                    response.setSuccess(true);
                    response.setOutTradeNo(outTradeNo);
                    response.setTradeNo(tradeNo);
                    response.setAmount(CurrencyTools.ofYuan(totalAmount));
                    response.setReplayMessage(AlipayConstants.REPLAY_SUCCESS);
                    tradeStatusDesc = AlipayConstants.TRADE_SUCCESS.equals(tradeStatus) ? "支付成功" : "交易结束";
                } else {
                    tradeStatusDesc = AlipayConstants.WAIT_BUYER_PAY.equals(tradeStatus)
                            ? "交易创建，等待买家付款"
                            : "未付款交易超时关闭，或支付完成后全额退款";
                    response.setErrorMsg(tradeStatusDesc);
                }
                if (logger.isInfoEnabled()) {
                    logger.info(request, "异步回调交易状态[{}]", tradeStatusDesc);
                }

            } else {
                response.setErrorMsg("异步回调验签失败");
                if (logger.isErrorEnabled()) {
                    logger.error(request, response.getErrorMsg());
                }
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("异步回调错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        AlipayTradeQueryModel queryModel = new AlipayTradeQueryModel();
        queryModel.setOutTradeNo(request.getOutTradeNo());

        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        queryRequest.setBizModel(queryModel);
        queryRequest.setReturnUrl(properties.getReturnUrl());
        queryRequest.setNotifyUrl(properties.getNotifyUrl());

        AlipayClient alipayClient = getAlipayClient();
        PaymentTradeQueryResponse response = new PaymentTradeQueryResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询支付交易请求参数：{}", PaymentUtils.toString(request));
            }

            AlipayTradeQueryResponse rsp = alipayClient.execute(queryRequest);
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询支付交易响应参数：{}", PaymentUtils.toString(rsp));
            }

            if (rsp.isSuccess()) {
                String tradeStatus = rsp.getTradeStatus();
                String tradeStatusDesc;
                if (AlipayConstants.TRADE_SUCCESS.equals(tradeStatus)
                        || AlipayConstants.TRADE_FINISHED.equals(tradeStatus)) {
                    response.setSuccess(true);
                    response.setOutTradeNo(rsp.getOutTradeNo());
                    response.setTradeNo(rsp.getTradeNo());
                    response.setAmount(CurrencyTools.ofYuan(rsp.getTotalAmount()));
                    tradeStatusDesc = AlipayConstants.TRADE_SUCCESS.equals(tradeStatus) ? "支付成功" : "交易结束";
                } else {
                    tradeStatusDesc = AlipayConstants.WAIT_BUYER_PAY.equals(tradeStatus)
                            ? "交易创建，等待买家付款"
                            : "未付款交易超时关闭，或支付完成后全额退款";
                    response.setErrorMsg(tradeStatusDesc);
                }
                response.setState(tradeStatus);
                if (logger.isInfoEnabled()) {
                    logger.info(request, "查询支付交易状态[{}]", tradeStatusDesc);
                }

            } else {
                if (AlipayConstants.QUERY_TRADE_NOT_EXIST.equals(rsp.getSubCode())) {
                    response.setNotExist(true);
                }
                response.setErrorMsg(rsp.getSubMsg());
                if (logger.isErrorEnabled()) {
                    logger.error(request, "查询支付交易失败：{}", response.getErrorMsg());
                }
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("查询支付交易结果错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        AlipayTradeRefundModel refundModel = new AlipayTradeRefundModel();
        refundModel.setOutTradeNo(request.getOutTradeNo());
        refundModel.setOutRequestNo(request.getOutRefundNo());
        refundModel.setRefundAmount(CurrencyTools.ofYuan(request.getRefundAmount()));
        refundModel.setRefundReason(request.getRefundReason());

        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        refundRequest.setBizModel(refundModel);

        AlipayClient alipayClient = getAlipayClient();
        PaymentTradeRefundResponse response = new PaymentTradeRefundResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "申请退款请求参数：{}", PaymentUtils.toString(request));
            }

            AlipayTradeRefundResponse rsp = alipayClient.execute(refundRequest);
            if (logger.isInfoEnabled()) {
                logger.info(request, "申请退款响应参数：{}", PaymentUtils.toString(rsp));
            }

            if (rsp.isSuccess()) {
                response.setSuccess(true);
                response.setOutTradeNo(request.getOutTradeNo());
                response.setOutRefundNo(request.getOutRefundNo());
                response.setTradeNo(rsp.getTradeNo());
                response.setRefundAmount(rsp.getRefundFee());
                response.setTotalAmount(request.getTotalAmount());
                if (logger.isInfoEnabled()) {
                    logger.info(request, "申请退款状态成功");
                }

            } else {
                if (AlipayConstants.QUERY_TRADE_NOT_EXIST.equals(rsp.getSubCode())) {
                    response.setNotExist(true);
                }
                response.setErrorMsg(rsp.getSubMsg());
                if (logger.isErrorEnabled()) {
                    logger.error(request, "申请退款失败：{}", response.getErrorMsg());
                }
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("申请退款错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        AlipayTradeFastpayRefundQueryModel refundQueryModel = new AlipayTradeFastpayRefundQueryModel();
        refundQueryModel.setOutTradeNo(request.getOutTradeNo());
        refundQueryModel.setOutRequestNo(request.getOutRefundNo());

        AlipayTradeFastpayRefundQueryRequest refundQueryRequest = new AlipayTradeFastpayRefundQueryRequest();
        refundQueryRequest.setBizModel(refundQueryModel);

        AlipayClient alipayClient = getAlipayClient();
        PaymentTradeRefundQueryResponse response = new PaymentTradeRefundQueryResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询退款请求参数：{}", PaymentUtils.toString(request));
            }

            AlipayTradeFastpayRefundQueryResponse rsp = alipayClient.execute(refundQueryRequest);
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询退款响应参数：{}", PaymentUtils.toString(rsp));
            }

            if (rsp.isSuccess()) {
                response.setSuccess(true);
                response.setOutTradeNo(request.getOutTradeNo());
                response.setOutRefundNo(request.getOutRefundNo());
                response.setTradeNo(rsp.getTradeNo());
                response.setRefundAmount(rsp.getRefundAmount());
                response.setTotalAmount(rsp.getTotalAmount());
                if (logger.isInfoEnabled()) {
                    logger.info(request, "查询退款状态成功");
                }

            } else {
                if (AlipayConstants.QUERY_TRADE_NOT_EXIST.equals(rsp.getSubCode())) {
                    response.setNotExist(true);
                }
                response.setErrorMsg(rsp.getSubMsg());
                if (logger.isErrorEnabled()) {
                    logger.error(request, "查询退款失败：{}", response.getErrorMsg());
                }
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("查询退款错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    public AlipayProperties getProperties() {
        return properties;
    }

    public void setProperties(AlipayProperties properties) {
        this.properties = properties;
    }

    public AlipayClient getAlipayClient() {
        return AlipayClientFactory.getInstance(properties);
    }

    public AlipayRequest<? extends AlipayResponse> getPayRequest(PaymentTradeRequest request) {
        throw new UnsupportedOperationException();
    }

    public <T extends AlipayResponse> T doPay(AlipayRequest<T> request) throws AlipayApiException {
        throw new UnsupportedOperationException();
    }

    protected void setPaySuccessResponse(PaymentTradeResponse response, AlipayResponse rsp) {
        response.setSuccess(true);
    }

    public static class Web extends AlipayTemplate implements WebTradeClientType {

        @Override
        public <T extends AlipayResponse> T doPay(AlipayRequest<T> request) throws AlipayApiException {
            return getAlipayClient().pageExecute(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, AlipayResponse rsp) {
            response.setSuccess(true);
            response.setForm(rsp.getBody());
        }

        @Override
        public AlipayTradePagePayRequest getPayRequest(PaymentTradeRequest request) {
            AlipayTradePagePayModel payModel = new AlipayTradePagePayModel();
            payModel.setOutTradeNo(request.getOutTradeNo());
            payModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            payModel.setSubject(request.getSubject());
            payModel.setBody(request.getBody());
            payModel.setProductCode(AlipayConstants.WEB_PRODUCT_CODE);

            AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
            payRequest.setBizModel(payModel);
            payRequest.setReturnUrl(properties.getReturnUrl());
            payRequest.setNotifyUrl(properties.getNotifyUrl());
            return payRequest;
        }

    }

    public static class Wap extends AlipayTemplate implements WapTradeClientType {
        @Override
        public <T extends AlipayResponse> T doPay(AlipayRequest<T> request) throws AlipayApiException {
            return getAlipayClient().pageExecute(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, AlipayResponse rsp) {
            response.setSuccess(true);
            response.setForm(rsp.getBody());
        }

        @Override
        public AlipayTradeWapPayRequest getPayRequest(PaymentTradeRequest request) {
            AlipayTradeWapPayModel payModel = new AlipayTradeWapPayModel();
            payModel.setOutTradeNo(request.getOutTradeNo());
            payModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            payModel.setSubject(request.getSubject());
            payModel.setBody(request.getBody());
            payModel.setQuitUrl(getAppProperties().getServerDomain());
            payModel.setProductCode(AlipayConstants.WAP_PRODUCT_CODE);

            AlipayTradeWapPayRequest payRequest = new AlipayTradeWapPayRequest();
            payRequest.setBizModel(payModel);
            payRequest.setReturnUrl(properties.getReturnUrl());
            payRequest.setNotifyUrl(properties.getNotifyUrl());
            return payRequest;
        }
    }

    public static class WebQrc extends AlipayTemplate implements WebQrcTradeClientType {

        @Override
        public <T extends AlipayResponse> T doPay(AlipayRequest<T> request) throws AlipayApiException {
            return getAlipayClient().pageExecute(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, AlipayResponse rsp) {
            response.setSuccess(true);
            response.setForm(rsp.getBody());
        }

        @Override
        public AlipayTradePagePayRequest getPayRequest(PaymentTradeRequest request) {
            AlipayTradePagePayModel payModel = new AlipayTradePagePayModel();
            payModel.setOutTradeNo(request.getOutTradeNo());
            payModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            payModel.setSubject(request.getSubject());
            payModel.setBody(request.getBody());
            payModel.setProductCode(AlipayConstants.WEB_PRODUCT_CODE);
            String size = request.get(PaymentConstants.QR_CODE_SIZE);
            long width;
            if (size == null || (width = Long.parseLong(size)) == 0L) {
                payModel.setQrPayMode("1");
            } else {
                payModel.setQrPayMode("4");
                payModel.setQrcodeWidth(width);
            }

            AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
            payRequest.setBizModel(payModel);
            payRequest.setNotifyUrl(properties.getNotifyUrl());
            return payRequest;
        }
    }

    public static class App extends AlipayTemplate implements AppTradeClientType {

        @Override
        public <T extends AlipayResponse> T doPay(AlipayRequest<T> request) throws AlipayApiException {
            return getAlipayClient().sdkExecute(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, AlipayResponse rsp) {
            response.setSuccess(true);
            response.putBody("orderInfo", rsp.getBody());
        }

        @Override
        public AlipayTradeAppPayRequest getPayRequest(PaymentTradeRequest request) {
            AlipayTradeAppPayModel payModel = new AlipayTradeAppPayModel();
            payModel.setOutTradeNo(request.getOutTradeNo());
            payModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            payModel.setSubject(request.getSubject());
            payModel.setBody(request.getBody());
            payModel.setProductCode(AlipayConstants.APP_PRODUCT_CODE);

            AlipayTradeAppPayRequest payRequest = new AlipayTradeAppPayRequest();
            payRequest.setBizModel(payModel);
            payRequest.setNotifyUrl(properties.getNotifyUrl());
            return payRequest;
        }
    }

    public static class Qrc extends AlipayTemplate implements QrcTradeClientType {
        @Override
        public <T extends AlipayResponse> T doPay(AlipayRequest<T> request) throws AlipayApiException {
            return getAlipayClient().execute(request);
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, AlipayResponse rsp) {
            response.setSuccess(true);
            response.setCodeUrl(((AlipayTradePrecreateResponse) rsp).getQrCode());
        }

        @Override
        public AlipayTradePrecreateRequest getPayRequest(PaymentTradeRequest request) {
            String storeId = request.get(PaymentConstants.STORE_ID);
            if (PaymentUtils.isBlankString(storeId)) {
                throw new IllegalArgumentException("门店编号不能为空");
            }

            AlipayTradePrecreateModel precreateModel = new AlipayTradePrecreateModel();
            precreateModel.setOutTradeNo(request.getOutTradeNo());
            precreateModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            precreateModel.setSubject(request.getSubject());
            precreateModel.setBody(request.getBody());
            precreateModel.setProductCode(AlipayConstants.F2F_PRODUCT_CODE);
            precreateModel.setStoreId(storeId);

            AlipayTradePrecreateRequest precreateRequest = new AlipayTradePrecreateRequest();
            precreateRequest.setNotifyUrl(properties.getNotifyUrl());
            precreateRequest.setBizModel(precreateModel);
            return precreateRequest;
        }
    }

    public static class F2f extends AlipayTemplate implements F2fTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            PaymentTradeResponse response = new PaymentTradeResponse();
            AlipayTradePayRequest payRequest = getPayRequest(request);
            boolean needQuery = false;

            try {
                if (logger.isInfoEnabled()) {
                    logger.info(request, "支付请求参数：{}", PaymentUtils.toString(payRequest));
                }

                AlipayTradePayResponse rsp = getAlipayClient().execute(payRequest);
                if (logger.isInfoEnabled()) {
                    logger.info(request, "支付响应参数：{}", PaymentUtils.toString(rsp));
                }

                String code = rsp.getCode();
                if (rsp.isSuccess() && "10000".equals(code)) {
                    response.setSuccess(true);
                    response.setTradeSuccess(true);
                    response.setTradeNo(rsp.getTradeNo());
                    response.setOutTradeNo(rsp.getOutTradeNo());
                } else if ("10003".equals(code) || "20000".equals(code)) {
                    needQuery = true;
                    response.setErrorMsg(rsp.getSubMsg());
                } else {
                    response.setErrorMsg("支付失败：" + rsp.getSubMsg());
                }
            } catch (AlipayApiException e) {
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

            if (reference.get().isSuccess()) {
                response.setSuccess(true);
                response.setTradeSuccess(true);
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

        private boolean cancel(PaymentTradeRequest request) {
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
        private boolean onceCancel(PaymentTradeRequest request) {
            AlipayTradeCancelModel cancelModel = new AlipayTradeCancelModel();
            cancelModel.setOutTradeNo(request.getOutTradeNo());
            AlipayTradeCancelRequest cancelRequest = new AlipayTradeCancelRequest();
            cancelRequest.setBizModel(cancelModel);
            try {
                if (logger.isInfoEnabled()) {
                    logger.info(request, "取消订单请求参数：{}", PaymentUtils.toString(cancelRequest));
                }

                AlipayTradeCancelResponse rsp = getAlipayClient().execute(cancelRequest);
                if (logger.isInfoEnabled()) {
                    logger.info(request, "取消订单响应参数：{}", PaymentUtils.toString(rsp));
                }

                if (!rsp.isSuccess()) {
                    if (logger.isErrorEnabled()) {
                        logger.error(request, "取消订单失败：{}", rsp.getSubMsg());
                    }
                    return false;
                }

                String retryFlag = rsp.getRetryFlag();
                if (!"N".equals(retryFlag)) {
                    if (logger.isErrorEnabled()) {
                        logger.error(request, "取消订单失败需要业务重试");
                    }
                    return false;
                }
                return true;
            } catch (AlipayApiException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(request, "取消订单错误：{}", e.getMessage());
                }
                return false;
            }
        }

        @Override
        public AlipayTradePayRequest getPayRequest(PaymentTradeRequest request) {
            String authCode = request.get(PaymentConstants.AUTH_CODE);
            if (PaymentUtils.isBlankString(authCode)) {
                throw new IllegalArgumentException("支付授权码不能为空");
            }
            AlipayTradePayModel payModel = new AlipayTradePayModel();
            payModel.setOutTradeNo(request.getOutTradeNo());
            payModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            payModel.setSubject(request.getSubject());
            payModel.setBody(request.getBody());
            payModel.setProductCode(AlipayConstants.F2F_PRODUCT_CODE);
            // 条形码
            payModel.setScene("bar_code");
            // 一分钟超时
            payModel.setTimeoutExpress("1m");
            payModel.setAuthCode(authCode);

            AlipayTradePayRequest payRequest = new AlipayTradePayRequest();
            payRequest.setBizModel(payModel);
            return payRequest;
        }
    }
}
