package com.test.payment.supplier.unionpay;

import com.test.payment.client.*;
import com.test.payment.domain.PaymentTradeQueryRequest;
import com.test.payment.domain.PaymentTradeQueryResponse;
import com.test.payment.domain.PaymentTradeRequest;
import com.test.payment.domain.PaymentTradeResponse;
import com.test.payment.properties.UnionpayProperties;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.unionpay.sdk.UnionpayClient;
import com.test.payment.supplier.unionpay.sdk.UnionpayConstants;
import com.test.payment.supplier.unionpay.sdk.UnionpayException;
import com.test.payment.supplier.unionpay.sdk.request.UnionpayTradeReversalRequest;
import com.test.payment.supplier.unionpay.sdk.request.UnionpayTradePayRequest;
import com.test.payment.supplier.wechat.sdk.WXPayConstants;
import com.test.payment.support.PaymentUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.test.payment.supplier.PaymentSupplierEnum.UNIONPAY;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayB2CTemplate extends UnionpayTemplate {

    private UnionpayProperties properties;

    @Override
    public PaymentSupplierEnum getSupplier() {
        return UNIONPAY;
    }

    @Override
    public String getBizType() {
        return UnionpayConstants.B2C;
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

    public static class Web extends UnionpayB2CTemplate implements WebTradeClientType {

        @Override
        public String getChannelType() {
            return UnionpayConstants.PC;
        }
    }

    public static class Wap extends UnionpayB2CTemplate implements WapTradeClientType {

        @Override
        public String getChannelType() {
            return UnionpayConstants.WAP;
        }
    }

    public static class App extends UnionpayB2CTemplate implements AppTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            UnionpayTradePayRequest unionpayRequest = getPayParams(request);
            PaymentTradeResponse response = new PaymentTradeResponse();
            try {
                logger.info(request, "预支付请求参数：{}", unionpayRequest);
                //网页支付
                Map<String, String> rsp = getUnionpayClient().appExecute(unionpayRequest);
                logger.info(request, "预支付响应参数：{}", rsp);
                if(UnionpayConstants.SUCCESS_CODE.equals(rsp.get("respCode"))) {
                    response.setSuccess(true);
                    response.setPrepayId(rsp.get("tn"));
                } else {
                    String respMsg = rsp.get("respMsg");
                    response.setErrorMsg(respMsg);
                    logger.info(request, "预支付请求失败：{}", respMsg);
                }
            } catch (UnionpayException e) {
                logger.error(request, "预支付错误：{}", e.getMessage());
                response.setErrorMsg("预支付失败：" + e.getMessage());
            }
            return response;
        }

        @Override
        public String getChannelType() {
            return UnionpayConstants.APP;
        }
    }

    public static class F2f extends UnionpayB2CTemplate implements F2fTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            UnionpayTradePayRequest unionpayRequest = getPayParams(request);
            PaymentTradeResponse response = new PaymentTradeResponse();
            try {
                logger.info(request, "支付请求参数：{}", unionpayRequest);
                //网页支付
                Map<String, String> rsp = getUnionpayClient().authCodeExecute(unionpayRequest);
                logger.info(request, "支付响应参数：{}", rsp);
                String respCode = rsp.get("respCode");
                if(UnionpayConstants.SUCCESS_CODE.equals(respCode)) {
                    response.setSuccess(true);
                    response.setTradeNo(rsp.get("queryId"));
                } else if ("03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode))  {
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
                    }, 5, 30);
                    PaymentTradeQueryResponse queryResponse = reference.get();

                    if (queryResponse.isSuccess()) {
                        response.setSuccess(true);
                        response.setTradeNo(queryResponse.getTradeNo());
                        response.setOutTradeNo(queryResponse.getOutTradeNo());
                    } else {
                        logger.info(request, "支付未完成正在取消订单");
                        if (cancel(request)) {
                            response.setErrorMsg("支付未完成已取订单");
                            logger.info(request, "取消订单失败");
                        } else {
                            response.setErrorMsg("支付未完成且取消订单异常");
                            logger.error(request, "取消订单失败");
                        }
                    }
                } else {
                    String respMsg = rsp.get("respMsg");
                    logger.info(request, "支付错误：{}", respMsg);
                    response.setErrorMsg(respMsg);
                }
            } catch (UnionpayException e) {
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

        public boolean onceCancel(PaymentTradeRequest request) {
            UnionpayTradeReversalRequest cancelRequest = new UnionpayTradeReversalRequest();
            cancelRequest.setOutTradeNo(request.getOutTradeNo());
            try {
                logger.info(request, "取消订单请求参数：{}", cancelRequest);
                Map<String, String> rsp = getUnionpayClient().reversal(cancelRequest);
                logger.info(request, "取消订单响应参数：{}", rsp);

                String returnCode = rsp.get("return_code");
                if (WXPayConstants.SUCCESS.equals(returnCode)) {
                    String resultCode = rsp.get("result_code");
                    if (!WXPayConstants.SUCCESS.equals(resultCode)) {
                        logger.info(request, "取消订单失败：{}", rsp.get("err_code_des"));
                        return false;
                    }
                    return true;
                } else {
                    logger.info(request, "取消订单请求失败：{}", rsp.get("return_msg"));
                    return false;
                }
            } catch (Exception e) {
                logger.error(request, "取消订单错误：{}", e.getMessage());
                return false;
            }
        }

        @Override
        protected UnionpayTradePayRequest getPayParams(PaymentTradeRequest request) {
            String authCode = request.get("authCode");
            if (PaymentUtils.isBlankString(authCode)) {
                throw new IllegalArgumentException("支付授权码为空");
            }
            UnionpayTradePayRequest unionpayRequest = super.getPayParams(request);
            unionpayRequest.setAuthCode(authCode);
            return unionpayRequest;
        }

        @Override
        public String getBizType() {
            return UnionpayConstants.CANCEL;
        }

        @Override
        public String getChannelType() {
            return UnionpayConstants.F2F;
        }
    }

    public static class Qrc extends UnionpayB2CTemplate implements QrcTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            UnionpayTradePayRequest unionpayRequest = getPayParams(request);
            PaymentTradeResponse response = new PaymentTradeResponse();
            try {
                logger.info(request, "预支付请求参数：{}", unionpayRequest);
                //网页支付
                Map<String, String> rsp = getUnionpayClient().qrCodeExecute(unionpayRequest);
                logger.info(request, "预支付响应参数：{}", rsp);
                if(UnionpayConstants.SUCCESS_CODE.equals(rsp.get("respCode"))) {
                    response.setSuccess(true);
                    response.setCodeUrl(rsp.get("qrCode"));
                } else {
                    String respMsg = rsp.get("respMsg");
                    response.setErrorMsg(respMsg);
                    logger.info(request, "预支付请求失败：{}", respMsg);
                }
            } catch (UnionpayException e) {
                logger.error(request, "预支付错误：{}", e.getMessage());
                response.setErrorMsg("预支付失败：" + e.getMessage());
            }
            return response;
        }

        @Override
        public String getChannelType() {
            return UnionpayConstants.F2F;
        }
    }
}

