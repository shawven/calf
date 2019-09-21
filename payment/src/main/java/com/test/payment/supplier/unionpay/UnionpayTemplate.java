package com.test.payment.supplier.unionpay;

import com.test.payment.client.WapTradeClientType;
import com.test.payment.client.WebTradeClientType;
import com.test.payment.domain.*;
import com.test.payment.properties.UnionpayProperties;
import com.test.payment.supplier.AbstractPaymentTemplate;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.unionpay.sdk.UnionpayClient;
import com.test.payment.supplier.unionpay.sdk.UnionpayException;
import com.test.payment.supplier.unionpay.sdk.domain.UnionpayTradePagePayRequest;
import com.test.payment.supplier.unionpay.sdk.domain.UnionpayTradeQueryRequest;
import com.test.payment.support.CurrencyTools;
import com.test.payment.support.PaymentUtils;

import java.util.Map;

import static com.test.payment.supplier.PaymentSupplierEnum.UNIONPAY;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayTemplate extends AbstractPaymentTemplate {

    protected UnionpayProperties properties;

    @Override
    public PaymentSupplierEnum getSupplier() {
        return UNIONPAY;
    }

    @Override
    public PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        PaymentTradeCallbackResponse response = new PaymentTradeCallbackResponse();
        Map<String, String> params = request.getParams();
        try {
            if (getUnionpayClient().verify(params)) {
                if ("00".equals(params.get("respCode"))) {
                    response.setSuccess(true);
                    response.setOutTradeNo(params.get("orderId"));
                    response.setTradeNo(params.get("queryId"));
                    response.setAmount(CurrencyTools.ofCent(params.get("txnAmt")));
                } else {
                    response.setErrorMsg(params.get("respMsg"));
                }
                logger.info(request, "同步跳转交易状态[{}]", params.get("respMsg"));
            } else {
                response.setErrorMsg("同步跳转验签失败");
                logger.info(request, "同步跳转验签失败");
            }
        } catch (UnionpayException e) {
            response.setErrorMsg("同步跳转错误：" + e.getMessage());
            logger.error(request, "同步跳转错误：{}", e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse asyncNotify(PaymentTradeCallbackRequest request) {
        PaymentTradeCallbackResponse response = new PaymentTradeCallbackResponse();
        Map<String, String> params = request.getParams();
        try {
            if (getUnionpayClient().verify(params)) {
                if ("00".equals(params.get("respCode"))) {
                    response.setSuccess(true);
                    response.setOutTradeNo(params.get("orderId"));
                    response.setTradeNo(params.get("queryId"));
                    response.setAmount(CurrencyTools.ofCent(params.get("txnAmt")));
                    response.setReplayMessage("ok");
                } else {
                    response.setErrorMsg(params.get("respMsg"));
                }
                logger.info(request, "异步回调交易状态[{}]", params.get("respMsg"));
            } else {
                response.setErrorMsg("异步回调验签失败");
                logger.info(request, "异步回调验签失败");
            }
        } catch (UnionpayException e) {
            response.setErrorMsg("异步回调错误：" + e.getMessage());
            logger.error(request, "异步回调错误：{}", e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        PaymentTradeQueryResponse response = new PaymentTradeQueryResponse();
        UnionpayTradeQueryRequest unionpayRequest = new UnionpayTradeQueryRequest();
        unionpayRequest.setOutTradeNo(request.getOutTradeNo());
        try {
            logger.info(request, "查询支付交易请求参数：{}", unionpayRequest);
            Map<String, String> rsp = getUnionpayClient().query(unionpayRequest);
            logger.info(request, "查询支付交易响应参数：{}", PaymentUtils.toString(rsp));

            //如果查询交易成功
            if("00".equals(rsp.get("respCode"))){
                //处理被查询交易的应答码逻辑
                String origRespCode = rsp.get("origRespCode");
                String respMsg = rsp.get("origRespMsg");
                // 交易成功
                if("00".equals(origRespCode)) {
                    response.setSuccess(true);
                    response.setTradeNo(rsp.get("queryId"));
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                } else if ("03".equals(origRespCode) || "04".equals(origRespCode) || "05".equals(origRespCode)){
                    response.setErrorMsg("等待支付完成");
                } else {
                    response.setErrorMsg(respMsg);
                }
                logger.info(request, "查询支付交易交易状态[{}]：{}", respMsg);
            } else {
                // 查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                String errorMsg = rsp.get("respMsg");
                logger.info(request, "查询支付交易请求失败：{}",errorMsg);
                response.setErrorMsg(errorMsg);
            }
        } catch (UnionpayException e) {
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

    public UnionpayClient getUnionpayClient() {
        return UnionpayClientFacotry.getInstance(properties);
    }

    public UnionpayProperties getProperties() {
        return properties;
    }

    public void setProperties(UnionpayProperties properties) {
        this.properties = properties;
    }

    public static class Web extends UnionpayTemplate implements WebTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            UnionpayTradePagePayRequest unionpayRequest = new UnionpayTradePagePayRequest();
            unionpayRequest.setOutTradeNo(request.getOutTradeNo());
            unionpayRequest.setAmount(CurrencyTools.toCent(request.getAmount()));
            unionpayRequest.setSubject(request.getSubject());
            unionpayRequest.setNotifyUrl(properties.getNotifyUrl());
            unionpayRequest.setReturnUrl(properties.getReturnUrl());

            PaymentTradeResponse response = new PaymentTradeResponse();
            try {
                logger.info(request, "预支付请求参数：{}", unionpayRequest);
                //网页支付
                String form = getUnionpayClient().pagePay(unionpayRequest);
                logger.info(request, "预支付响应参数：{}", unionpayRequest);
                response.setSuccess(true);
                response.putBody("form", form);
            } catch (UnionpayException e) {
                logger.error(request, "预支付错误：{}", e.getMessage());
                response.setErrorMsg("预支付失败：" + e.getMessage());
            }
            return response;
        }
    }

    public static class Wap extends UnionpayTemplate implements WapTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            return null;
        }
    }
}
