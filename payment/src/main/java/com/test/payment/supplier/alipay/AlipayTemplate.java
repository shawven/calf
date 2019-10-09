package com.test.payment.supplier.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.test.payment.client.*;
import com.test.payment.domain.*;
import com.test.payment.properties.AlipayProperties;
import com.test.payment.supplier.AbstractPaymentTemplate;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.alipay.sdk.AlipayConstants;
import com.test.payment.support.CurrencyTools;
import com.test.payment.support.PaymentUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.test.payment.supplier.PaymentSupplierEnum.ALIPAY;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class AlipayTemplate  extends AbstractPaymentTemplate {

    protected AlipayProperties properties;

    @Override
    public PaymentSupplierEnum getSupplier() {
        return ALIPAY;
    }

    @Override
    public PaymentTradeResponse pay(PaymentTradeRequest request) {
        PaymentTradeResponse response = new PaymentTradeResponse();
        AlipayRequest<? extends AlipayResponse> alipayRequest = getAlipayRequest(request);
        try {
            logger.info(request, "预支付请求参数：{}", PaymentUtils.toString(alipayRequest));
            //网页支付
            AlipayResponse rsp = getAlipayClient().pageExecute(alipayRequest);
            logger.info(request, "预支付响应参数：{}", PaymentUtils.toString(response));

            if (rsp.isSuccess()) {
                response.setSuccess(true);
                response.setForm(rsp.getBody());
            } else {
                logger.error(request, "预支付请求失败：{}", rsp.getMsg());
                response.setErrorMsg(rsp.getSubMsg());
            }
        } catch (AlipayApiException e) {
            logger.error(request, "预支付错误：{}", e.getMessage());
            response.setErrorMsg("预支付失败：" + e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        PaymentTradeCallbackResponse response = new PaymentTradeCallbackResponse();
        try {
            Map<String, String> params = request.getParams();
            logger.info(request, "同步回调接受参数：{}", params);
            boolean validation = AlipaySignature.rsaCheckV1(params, properties.getPublicKey(),
                    properties.getCharset(), properties.getSignType());
            if (validation) {
                String tradeNo = params.get("trade_no");
                // 新版不支持交易状态 转用查询
                logger.info(request, "同步回调转查询交易：{}", params);

                PaymentTradeQueryRequest queryRequest = new PaymentTradeQueryRequest(request);
                queryRequest.setTradeNo(tradeNo);
                PaymentTradeQueryResponse queryResponse = query(queryRequest);

                response.setSuccess(queryResponse.isSuccess());
                response.setAmount(CurrencyTools.ofYuan(queryResponse.getAmount()));
                response.setTradeNo(queryResponse.getTradeNo());
                response.setOutTradeNo(queryResponse.getOutTradeNo());
            }else{
                response.setErrorMsg("同步回调验签失败");
                logger.error(request, "同步回调验签失败");
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("同步跳转错误：" + e.getMessage());
            logger.error(request, "同步跳转错误：{}", e.getErrMsg());
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
                logger.info(request, "异步回调交易状态[{}]", tradeStatusDesc);
            } else{
                response.setErrorMsg("异步回调验签失败");
                logger.error(request, "异步回调验签失败");
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("异步回调错误：" + e.getMessage());
            logger.error(request, "异步回调错误：{}", e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        AlipayTradeQueryModel alipayModel = new AlipayTradeQueryModel();
        alipayModel.setTradeNo(request.getTradeNo());

        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
        alipayRequest.setBizModel(alipayModel);
        alipayRequest.setReturnUrl(properties.getReturnUrl());
        alipayRequest.setNotifyUrl(properties.getNotifyUrl());

        AlipayClient alipayClient = getAlipayClient();
        PaymentTradeQueryResponse response = new PaymentTradeQueryResponse();
        try {
            logger.info(request, "查询支付交易请求参数：{}", PaymentUtils.toString(request));
            AlipayTradeQueryResponse rsp = alipayClient.execute(alipayRequest);
            logger.info(request, "查询支付交易响应参数：{}", PaymentUtils.toString(rsp));
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
                logger.info(request, "查询支付交易状态[{}]", tradeStatusDesc);
            } else {
                logger.error(request, "查询支付交易请求失败：{}", rsp.getMsg());
                response.setErrorMsg(rsp.getSubMsg());
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("查询支付交易结果错误：" + e.getMessage());
            logger.error(request, "查询支付交易结果错误：{}", e.getMessage());
        }
        return response;
    }


    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        AlipayTradeRefundModel alipayModel = new AlipayTradeRefundModel();
        alipayModel.setTradeNo(request.getTradeNo());
        alipayModel.setOutRequestNo(request.getOutRefundNo());
        alipayModel.setRefundAmount(CurrencyTools.ofYuan(request.getRefundAmount()));
        alipayModel.setRefundReason(request.getRefundReason());

        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
        alipayRequest.setBizModel(alipayModel);

        AlipayClient alipayClient = getAlipayClient();
        PaymentTradeRefundResponse response = new PaymentTradeRefundResponse();
        try {
            logger.info(request, "申请退款请求参数：{}", PaymentUtils.toString(request));
            AlipayTradeRefundResponse rsp = alipayClient.execute(alipayRequest);
            logger.info(request, "申请退款响应参数：{}", PaymentUtils.toString(rsp));
            if (rsp.isSuccess()) {
                response.setSuccess(true);
                response.setOutTradeNo(rsp.getOutTradeNo());
                response.setOutRefundNo(request.getOutRefundNo());
                response.setTradeNo(rsp.getTradeNo());
                response.setRefundAmount(rsp.getRefundFee());
                response.setTotalAmount(request.getTotalAmount());
                logger.info(request, "申请退款状态成功");
            } else {
                logger.error(request, "申请退款请求失败：{}", rsp.getMsg());
                response.setErrorMsg(rsp.getSubMsg());
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("申请退款错误：" + e.getMessage());
            logger.error(request, "申请退款错误：{}", e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        AlipayTradeFastpayRefundQueryModel alipayModel = new AlipayTradeFastpayRefundQueryModel();
        alipayModel.setTradeNo(request.getTradeNo());
        alipayModel.setOutRequestNo(request.getOutRefundNo());

        AlipayTradeFastpayRefundQueryRequest alipayRequest = new AlipayTradeFastpayRefundQueryRequest();
        alipayRequest.setBizModel(alipayModel);

        AlipayClient alipayClient = getAlipayClient();
        PaymentTradeRefundQueryResponse response = new PaymentTradeRefundQueryResponse();
        try {
            logger.info(request, "查询退款请求参数：{}", PaymentUtils.toString(request));
            AlipayTradeFastpayRefundQueryResponse rsp = alipayClient.execute(alipayRequest);
            logger.info(request, "查询退款响应参数：{}", PaymentUtils.toString(rsp));
            if (rsp.isSuccess()) {
                response.setSuccess(true);
                response.setOutTradeNo(rsp.getOutTradeNo());
                response.setOutRefundNo(rsp.getOutRequestNo());
                response.setTradeNo(rsp.getTradeNo());
                response.setRefundAmount(rsp.getRefundAmount());
                response.setTotalAmount(rsp.getTotalAmount());
                logger.info(request, "查询退款状态成功");
            } else {
                logger.error(request, "查询退款请求失败：{}", rsp.getMsg());
                response.setErrorMsg(rsp.getSubMsg());
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("查询退款错误：" + e.getMessage());
            logger.error(request, "查询退款错误：{}", e.getMessage());
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

    public abstract AlipayRequest<? extends AlipayResponse> getAlipayRequest(PaymentTradeRequest request);

    public static class Web extends AlipayTemplate implements WebTradeClientType {

        @Override
        public AlipayTradePagePayRequest getAlipayRequest(PaymentTradeRequest request) {
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            AlipayTradePagePayModel alipayModel = new AlipayTradePagePayModel();
            alipayModel.setOutTradeNo(request.getOutTradeNo());
            alipayModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            alipayModel.setSubject(request.getSubject());
            alipayModel.setBody(request.getBody());
            alipayModel.setProductCode(AlipayConstants.WAP_PRODUCT_CODE);

            alipayRequest.setBizModel(alipayModel);
            alipayRequest.setReturnUrl(properties.getReturnUrl());
            alipayRequest.setNotifyUrl(properties.getNotifyUrl());
            return alipayRequest;
        }
    }

    public static class Wap extends AlipayTemplate implements WapTradeClientType {

        @Override
        public AlipayTradeWapPayRequest getAlipayRequest(PaymentTradeRequest request) {
            AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
            AlipayTradeWapPayModel alipayModel = new AlipayTradeWapPayModel();
            alipayModel.setOutTradeNo(request.getOutTradeNo());
            alipayModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            alipayModel.setSubject(request.getSubject());
            alipayModel.setBody(request.getBody());
            alipayModel.setQuitUrl(getGlobalProperties().getServerDomain());
            alipayModel.setProductCode(AlipayConstants.WAP_PRODUCT_CODE);

            alipayRequest.setBizModel(alipayModel);
            alipayRequest.setReturnUrl(properties.getReturnUrl());
            alipayRequest.setNotifyUrl(properties.getNotifyUrl());
            return alipayRequest;
        }
    }

    public static class WebQrc extends AlipayTemplate implements WebQrcTradeClientType {

        @Override
        public AlipayTradePagePayRequest getAlipayRequest(PaymentTradeRequest request) {
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            AlipayTradePagePayModel alipayModel = new AlipayTradePagePayModel();
            alipayModel.setOutTradeNo(request.getOutTradeNo());
            alipayModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            alipayModel.setSubject(request.getSubject());
            alipayModel.setBody(request.getBody());
            alipayModel.setProductCode(AlipayConstants.WEB_PRODUCT_CODE);

            String width = request.get("width");
            long l;
            if (width == null || (l = Long.parseLong(width)) == 0L) {
                alipayModel.setQrPayMode("1");
            } else {
                alipayModel.setQrPayMode("4");
                alipayModel.setQrcodeWidth(l);
            }
            alipayRequest.setBizModel(alipayModel);
            alipayRequest.setNotifyUrl(properties.getNotifyUrl());
            return alipayRequest;
        }
    }

    public static class App extends AlipayTemplate implements AppTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            PaymentTradeResponse response = new PaymentTradeResponse();
            AlipayTradeAppPayRequest alipayRequest = getAlipayRequest(request);
            try {
                logger.info(request, "预支付请求参数：{}", PaymentUtils.toString(alipayRequest));
                AlipayResponse rsp = getAlipayClient().sdkExecute(alipayRequest);
                logger.info(request, "预支付响应参数：{}", PaymentUtils.toString(response));

                if (rsp.isSuccess()) {
                    response.setSuccess(true);
                    response.putBody("orderInfo", rsp.getBody());
                } else {
                    logger.error(request, "预支付请求失败：{}", rsp.getMsg());
                    response.setErrorMsg(rsp.getSubMsg());
                }
            } catch (AlipayApiException e) {
                logger.error(request, "预支付错误：{}", e.getMessage());
                response.setErrorMsg("预支付失败：" + e.getMessage());
            }
            return response;
        }

        @Override
        public AlipayTradeAppPayRequest getAlipayRequest(PaymentTradeRequest request) {
            AlipayTradeAppPayRequest alipayRequest = new AlipayTradeAppPayRequest();
            AlipayTradeAppPayModel alipayModel = new AlipayTradeAppPayModel();
            alipayModel.setOutTradeNo(request.getOutTradeNo());
            alipayModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            alipayModel.setSubject(request.getSubject());
            alipayModel.setBody(request.getBody());
            alipayModel.setProductCode(AlipayConstants.APP_PRODUCT_CODE);
            alipayRequest.setBizModel(alipayModel);
            alipayRequest.setNotifyUrl(properties.getNotifyUrl());
            return alipayRequest;
        }
    }

    public static class F2f extends AlipayTemplate implements F2fTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            PaymentTradeResponse response = new PaymentTradeResponse();
            AlipayTradePayRequest alipayRequest = getAlipayRequest(request);
            try {
                logger.info(request, "支付请求参数：{}", PaymentUtils.toString(alipayRequest));
                AlipayTradePayResponse rsp = getAlipayClient().execute(alipayRequest);
                logger.info(request, "支付响应参数：{}", PaymentUtils.toString(response));

                String code = rsp.getCode();
                if (rsp.isSuccess() && "10000".equals(code)) {
                    response.setSuccess(true);
                    response.setTradeSuccess(true);
                    response.setTradeNo(rsp.getTradeNo());
                    response.setOutTradeNo(rsp.getOutTradeNo());
                } else if ("10003".equals(code)) {
                    logger.info(request, "等待支付完成正在轮训查询订单");
                    AtomicReference<PaymentTradeQueryResponse> reference = new AtomicReference<>(new PaymentTradeQueryResponse());
                    PaymentUtils.schedule(new PaymentUtils.FutureRunnable() {
                        @Override
                        public void run() {
                            PaymentTradeQueryRequest queryRequest = new PaymentTradeQueryRequest(request);
                            queryRequest.setOutTradeNo(request.getOutTradeNo());
                            PaymentTradeQueryResponse result = query(queryRequest);
                            if (result.isSuccess()) {
                                this.cancel();
                            }
                            reference.set(result);
                        }
                    }, 5, 60);
                    PaymentTradeQueryResponse queryResponse = reference.get();

                    if (reference.get().isSuccess()) {
                        response.setSuccess(true);
                        response.setTradeSuccess(true);
                        response.setTradeNo(queryResponse.getTradeNo());
                        response.setOutTradeNo(queryResponse.getOutTradeNo());
                    } else {
                        if (cancel(request)) {
                            response.setErrorMsg("支付未完成已取订单");
                            logger.info(request, "取消订单失败");
                        } else {
                            response.setErrorMsg("支付未完成且取消订单异常");
                            logger.error(request, "取消订单失败");
                        }
                    }
                } else {
                    logger.error(request, "支付请求失败：{}", rsp.getMsg());
                    response.setErrorMsg(rsp.getSubMsg());
                }
            } catch (AlipayApiException e) {
                logger.error(request, "支付错误：{}", e.getMessage());
                response.setErrorMsg("支付失败：" + e.getMessage());
            }
            return response;
        }

        public boolean cancel(PaymentTradeRequest request) {
            AtomicReference<Boolean> reference = new AtomicReference<>(false);
            PaymentUtils.schedule(() -> {
                boolean b = onceCancel(request);
                reference.set(b);
                return !b;
            }, () -> logger.info(request, "正在重试取消订单"), 5);
            return reference.get();
        }

        /**
         * @param request
         * @return 成功与否
         */
        public boolean onceCancel(PaymentTradeRequest request) {
            AlipayTradeCancelRequest cancelRequest = new AlipayTradeCancelRequest();
            AlipayTradeCancelModel cancelModel = new AlipayTradeCancelModel();
            cancelModel.setOutTradeNo(request.getOutTradeNo());
            cancelRequest.setBizModel(cancelModel);
            try {
                logger.info(request, "取消订单请求参数：{}", PaymentUtils.toString(cancelRequest));
                AlipayTradeCancelResponse cancelResponse = getAlipayClient().execute(cancelRequest);
                logger.info(request, "取消订单响应参数：{}", PaymentUtils.toString(cancelResponse));

                if (!cancelResponse.isSuccess()) {
                    logger.error(request, "取消订单失败：{}", cancelResponse.getSubMsg());
                    return false;
                }

                String retryFlag = cancelResponse.getRetryFlag();
                if (!"N".equals(retryFlag)) {
                    logger.error(request, "取消订单失败需要重试请求");
                    return false;
                }
                return true;
            } catch (AlipayApiException e) {
                logger.error(request, "取消订单错误：{}", e.getMessage());
                return false;
            }
        }


        @Override
        public AlipayTradePayRequest getAlipayRequest(PaymentTradeRequest request) {
            String authCode = request.get("authCode");
            if (PaymentUtils.isBlankString(authCode)) {
                throw new IllegalArgumentException("支付授权码为空");
            }
            AlipayTradePayRequest alipayRequest = new AlipayTradePayRequest();
            AlipayTradePayModel alipayModel = new AlipayTradePayModel();
            alipayModel.setOutTradeNo(request.getOutTradeNo());
            alipayModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            alipayModel.setSubject(request.getSubject());
            alipayModel.setBody(request.getBody());
            alipayModel.setProductCode(AlipayConstants.F2F_PRODUCT_CODE);
            // 条形码
            alipayModel.setScene("bar_code");
            // 一分钟超时
            alipayModel.setTimeoutExpress("1m");
            alipayModel.setAuthCode(authCode);
            alipayRequest.setBizModel(alipayModel);
            return alipayRequest;
        }
    }

    public static class Qrc extends AlipayTemplate implements QrcTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            PaymentTradeResponse response = new PaymentTradeResponse();
            AlipayTradePrecreateRequest alipayRequest = getAlipayRequest(request);
            try {
                logger.info(request, "预支付请求参数：{}", PaymentUtils.toString(alipayRequest));
                AlipayTradePrecreateResponse rsp = getAlipayClient().execute(alipayRequest);
                logger.info(request, "预支付响应参数：{}", PaymentUtils.toString(response));

                if (rsp.isSuccess()) {
                    response.setSuccess(true);
                    response.setCodeUrl(rsp.getQrCode());
                } else {
                    logger.error(request, "预支付请求失败：{}", rsp.getMsg());
                    response.setErrorMsg(rsp.getSubMsg());
                }
            } catch (AlipayApiException e) {
                logger.error(request, "预支付错误：{}", e.getMessage());
                response.setErrorMsg("预支付失败：" + e.getMessage());
            }
            return response;
        }

        @Override
        public AlipayTradePrecreateRequest getAlipayRequest(PaymentTradeRequest request) {
            AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();
            AlipayTradePrecreateModel alipayModel = new AlipayTradePrecreateModel();
            alipayModel.setOutTradeNo(request.getOutTradeNo());
            alipayModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            alipayModel.setSubject(request.getSubject());
            alipayModel.setBody(request.getBody());
            alipayModel.setProductCode(AlipayConstants.APP_PRODUCT_CODE);
            alipayRequest.setBizModel(alipayModel);
            alipayRequest.setNotifyUrl(properties.getNotifyUrl());
            return alipayRequest;
        }
    }
}
