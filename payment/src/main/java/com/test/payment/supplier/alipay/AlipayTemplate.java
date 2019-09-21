package com.test.payment.supplier.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.test.payment.client.QrcTradeClientType;
import com.test.payment.client.WapTradeClientType;
import com.test.payment.client.WebTradeClientType;
import com.test.payment.domain.*;
import com.test.payment.properties.AlipayProperties;
import com.test.payment.supplier.AbstractPaymentTemplate;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.support.CurrencyTools;
import com.test.payment.support.PaymentUtils;

import java.util.Map;

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
            AlipayResponse alipayResponse = getAlipayClient().pageExecute(alipayRequest);
            logger.info(request, "预支付响应参数：{}", PaymentUtils.toString(response));

            if (alipayResponse.isSuccess()) {
                response.setSuccess(true);
                response.putBody("form", alipayResponse.getBody());
            } else {
                logger.error(request, "预支付请求失败：{}", alipayResponse.getMsg());
                response.setErrorMsg(alipayResponse.getSubMsg());
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
                String outTradeNo = params.get("out_trade_no");
                // 新版不支持交易状态 转用查询
                logger.info(request, "同步回调转查询交易：{}", params);

                PaymentTradeQueryRequest queryRequest = new PaymentTradeQueryRequest(request);
                queryRequest.setOutTradeNo(outTradeNo);
                PaymentTradeQueryResponse queryResponse = query(queryRequest);

                response.setSuccess(queryResponse.isSuccess());
                response.setAmount(CurrencyTools.ofYuan(queryResponse.getAmount()));
                response.setTradeNo(queryResponse.getTradeNo());
                response.setOutTradeNo(outTradeNo);
            }else{
                response.setErrorMsg("同步回调验签失败");
                logger.info(request, "同步回调验签失败");
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
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    response.setSuccess(true);
                    response.setOutTradeNo(outTradeNo);
                    response.setTradeNo(tradeNo);
                    response.setAmount(CurrencyTools.ofYuan(totalAmount));
                    response.setReplayMessage("success");
                    tradeStatusDesc = "TRADE_SUCCESS".equals(tradeStatus) ? "支付成功" : "交易结束";
                } else {
                    tradeStatusDesc = "WAIT_BUYER_PAY".equals(tradeStatus)
                            ? "交易创建，等待买家付款"
                            : "未付款交易超时关闭，或支付完成后全额退款";
                    response.setErrorMsg(tradeStatusDesc);
                }
                logger.info(request, "异步回调交易状态[{}]", tradeStatusDesc);
            } else{
                response.setErrorMsg("异步回调验签失败");
                logger.info(request, "异步回调验签失败");
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
        alipayModel.setOutTradeNo(request.getOutTradeNo());

        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
        alipayRequest.setBizModel(alipayModel);
        alipayRequest.setReturnUrl(properties.getReturnUrl());
        alipayRequest.setNotifyUrl(properties.getNotifyUrl());

        AlipayClient alipayClient = getAlipayClient();
        PaymentTradeQueryResponse response = new PaymentTradeQueryResponse();
        try {
            logger.info(request, "查询支付交易请求参数：{}", PaymentUtils.toString(request));
            AlipayTradeQueryResponse alipayResponse = alipayClient.execute(alipayRequest);
            logger.info(request, "查询支付交易响应参数：{}", PaymentUtils.toString(alipayResponse));
            if (alipayResponse.isSuccess()) {
                String tradeStatus = alipayResponse.getTradeStatus();
                String tradeStatusDesc;
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    response.setSuccess(true);
                    response.setOutTradeNo(alipayResponse.getOutTradeNo());
                    response.setTradeNo(alipayResponse.getTradeNo());
                    response.setAmount(CurrencyTools.ofYuan(alipayResponse.getTotalAmount()));
                    tradeStatusDesc = "TRADE_SUCCESS".equals(tradeStatus) ? "支付成功" : "交易结束";
                } else {
                    tradeStatusDesc = "WAIT_BUYER_PAY".equals(tradeStatus)
                            ? "交易创建，等待买家付款"
                            : "未付款交易超时关闭，或支付完成后全额退款";
                    response.setErrorMsg(tradeStatusDesc);
                }
                logger.info(request, "查询支付交易状态[{}]", tradeStatusDesc);
            } else {
                logger.error(request, "查询支付交易请求失败：{}", alipayResponse.getMsg());
                response.setErrorMsg(alipayResponse.getSubMsg());
            }
        } catch (AlipayApiException e) {
            response.setErrorMsg("查询支付交易结果错误：" + e.getMessage());
            logger.info(request, "查询支付交易结果错误：{}", e.getMessage());
        }
        return response;
    }


    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        return null;
    }

    @Override
    public PaymentTradeRefundQueryResponse queryRefund(PaymentTradeRefundQueryRequest request) {
        return null;
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
            alipayModel.setProductCode("FAST_INSTANT_TRADE_PAY");

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
            alipayModel.setProductCode("QUICK_WAP_WAY");

            alipayRequest.setBizModel(alipayModel);
            alipayRequest.setReturnUrl(properties.getReturnUrl());
            alipayRequest.setNotifyUrl(properties.getNotifyUrl());
            return alipayRequest;
        }
    }

    public static class Qrc extends AlipayTemplate implements QrcTradeClientType {

        @Override
        public AlipayTradePagePayRequest getAlipayRequest(PaymentTradeRequest request) {
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            AlipayTradePagePayModel alipayModel = new AlipayTradePagePayModel();
            alipayModel.setOutTradeNo(request.getOutTradeNo());
            alipayModel.setTotalAmount(CurrencyTools.toYuan(request.getAmount()));
            alipayModel.setSubject(request.getSubject());
            alipayModel.setBody(request.getBody());
            alipayModel.setProductCode("FAST_INSTANT_TRADE_PAY");

            String width = request.getOption().get("width");
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
}
