package com.test.payment.supplier.unionpay;

import com.test.payment.client.WapTradeClientType;
import com.test.payment.client.WebTradeClientType;
import com.test.payment.domain.*;
import com.test.payment.properties.UnionpayProperties;
import com.test.payment.supplier.AbstractPaymentTemplate;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.unionpay.sdk.UnionpayClient;
import com.test.payment.supplier.unionpay.sdk.UnionpayConstants;
import com.test.payment.supplier.unionpay.sdk.UnionpayException;
import com.test.payment.supplier.unionpay.sdk.request.UnionpayTradePagePayRequest;
import com.test.payment.supplier.unionpay.sdk.request.UnionpayTradeQueryRequest;
import com.test.payment.supplier.unionpay.sdk.request.UnionpayTradeRefundQueryRequest;
import com.test.payment.supplier.unionpay.sdk.request.UnionpayTradeRefundRequest;
import com.test.payment.support.CurrencyTools;

import java.util.Map;

import static com.test.payment.supplier.PaymentSupplierEnum.UNIONPAY;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayTemplate extends AbstractPaymentTemplate {

    @Override
    public PaymentTradeResponse pay(PaymentTradeRequest request) {
        UnionpayTradePagePayRequest unionpayRequest = new UnionpayTradePagePayRequest();
        unionpayRequest.setBizType(getBizType());
        unionpayRequest.setChannelType(getChannelType());

        unionpayRequest.setOutTradeNo(request.getOutTradeNo());
        unionpayRequest.setAmount(CurrencyTools.toCent(request.getAmount()));
        unionpayRequest.setSubject(request.getSubject());
        unionpayRequest.setNotifyUrl(getProperties().getNotifyUrl());
        unionpayRequest.setReturnUrl(getProperties().getReturnUrl());

        PaymentTradeResponse response = new PaymentTradeResponse();
        try {
            logger.info(request, "预支付请求参数：{}", unionpayRequest);
            //网页支付
            String form = getUnionpayClient().pagePay(unionpayRequest);
            logger.info(request, "预支付响应参数：{}", form);
            response.setSuccess(true);
            response.putBody("form", form);
        } catch (UnionpayException e) {
            logger.error(request, "预支付错误：{}", e.getMessage());
            response.setErrorMsg("预支付失败：" + e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        PaymentTradeCallbackResponse response = new PaymentTradeCallbackResponse();
        Map<String, String> params = request.getParams();
        try {
            logger.info(request, "同步回调接受参数：{}", params);
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
                logger.error(request, "同步跳转验签失败");
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
            logger.info(request, "异步回调接受参数：{}", params);
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
                logger.error(request, "异步回调验签失败");
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
        unionpayRequest.setBizType(getBizType());
        unionpayRequest.setOutTradeNo(request.getOutTradeNo());
        try {
            logger.info(request, "查询支付交易请求参数：{}", unionpayRequest);
            Map<String, String> rsp = getUnionpayClient().query(unionpayRequest);
            logger.info(request, "查询支付交易响应参数：{}", rsp);

            //如果查询交易成功
            if("00".equals(rsp.get("respCode"))){
                //处理被查询交易的应答码逻辑
                String origRespCode = rsp.get("origRespCode");
                String respMsg = rsp.get("origRespMsg");
                // 交易成功
                if("00".equals(origRespCode)) {
                    response.setSuccess(true);
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setTradeNo(rsp.get("queryId"));
                    response.setAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                    respMsg = "成功";
                } else if ("03".equals(origRespCode) || "04".equals(origRespCode) || "05".equals(origRespCode)){
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setTradeNo(rsp.get("queryId"));
                    response.setAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                    response.setErrorMsg("等待支付完成");
                } else {
                    response.setErrorMsg(respMsg);
                }
                logger.info(request, "查询支付交易状态[{}]", respMsg);
            } else {
                // 查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                String errorMsg = rsp.get("respMsg");
                logger.info(request, "查询支付交易请求失败：{}",errorMsg);
                response.setErrorMsg(errorMsg);
            }
        } catch (UnionpayException e) {
            response.setErrorMsg("查询支付交易结果错误：" + e.getMessage());
            logger.error(request, "查询支付交易结果错误：{}", e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        UnionpayTradeRefundRequest unionpayRequest = new UnionpayTradeRefundRequest();
        unionpayRequest.setBizType(getBizType());
        unionpayRequest.setOutRefundNo(request.getOutRefundNo());
        unionpayRequest.setTradeNo(request.getTradeNo());
        unionpayRequest.setRefundAmount(CurrencyTools.toCent(request.getRefundAmount()));

        PaymentTradeRefundResponse response = new PaymentTradeRefundResponse();
        try {
            logger.info(request, "申请退款请求参数：{}", unionpayRequest);
            Map<String, String> rsp = getUnionpayClient().refund(unionpayRequest);
            logger.info(request, "申请退款响应参数：{}", rsp);

            String respCode = rsp.get("respCode");
            String respMsg = rsp.get("respMsg");
            //如果查询交易成功
            if("00".equals(respCode)){
                // 交易成功
                response.setSuccess(true);
                response.setOutTradeNo(request.getOutTradeNo());
                response.setOutRefundNo(request.getOutRefundNo());
                response.setTradeNo(request.getTradeNo());
                response.setRefundNo(rsp.get("queryId"));
                response.setRefundAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                response.setTotalAmount(request.getTotalAmount());
                respMsg = "成功";
            } else if ("03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode)){
                response.setErrorMsg("等待退款完成");
            } else {
                // 查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                response.setErrorMsg(respMsg);
            }
            logger.info(request, "申请退款状态[{}]", respMsg);
        } catch (UnionpayException e) {
            response.setErrorMsg("申请退款错误：" + e.getMessage());
            logger.error(request, "申请退款错误：{}", e.getMessage());
        }
        return response;
    }

    @Override
    public PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        UnionpayTradeRefundQueryRequest unionpayRequest = new UnionpayTradeRefundQueryRequest();
        unionpayRequest.setBizType(getBizType());
        unionpayRequest.setOutRefundNo(request.getOutRefundNo());

        PaymentTradeRefundQueryResponse response = new PaymentTradeRefundQueryResponse();
        try {
            logger.info(request, "查询退款请求参数：{}", unionpayRequest);
            Map<String, String> rsp = getUnionpayClient().refundQuery(unionpayRequest);
            logger.info(request, "查询退款响应参数：{}", rsp);

            //如果查询交易成功
            if("00".equals(rsp.get("respCode"))){
                //处理被查询交易的应答码逻辑
                String origRespCode = rsp.get("origRespCode");
                String respMsg = rsp.get("origRespMsg");
                // 交易成功
                if("00".equals(origRespCode)) {
                    response.setSuccess(true);
                    response.setOutRefundNo(request.getOutRefundNo());
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setTradeNo(request.getTradeNo());
                    response.setRefundNo(rsp.get("queryId"));
                    response.setTotalAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                    respMsg = "成功";
                } else if ("03".equals(origRespCode) || "04".equals(origRespCode) || "05".equals(origRespCode)){
                    response.setErrorMsg("等待退款完成");
                } else {
                    response.setErrorMsg(respMsg);
                }
                logger.info(request, "查询退款状态[{}]", respMsg);
            } else {
                // 查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                String errorMsg = rsp.get("respMsg");
                logger.info(request, "查询退款请求失败：{}",errorMsg);
                response.setErrorMsg(errorMsg);
            }
        } catch (UnionpayException e) {
            response.setErrorMsg("查询退款错误：" + e.getMessage());
            logger.error(request, "查询退款错误：{}", e.getMessage());
        }
        return response;
    }

    public abstract UnionpayClient getUnionpayClient();

    public abstract void setProperties(UnionpayProperties properties);

    public abstract UnionpayProperties getProperties();

    public abstract String getBizType();

    public abstract String getChannelType();
}
