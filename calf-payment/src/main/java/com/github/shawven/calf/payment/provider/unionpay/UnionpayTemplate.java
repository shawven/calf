package com.github.shawven.calf.payment.provider.unionpay;

import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayClient;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayConstants;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayException;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradePayRequest;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradeQueryRequest;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradeRefundQueryRequest;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.UnionpayTradeRefundRequest;
import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.properties.UnionpayProperties;
import com.github.shawven.calf.payment.provider.AbstractPaymentTemplate;
import com.github.shawven.calf.payment.support.CurrencyTools;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-08-27
 */
public abstract class UnionpayTemplate extends AbstractPaymentTemplate {

    @Override
    public PaymentTradeResponse pay(PaymentTradeRequest request) {
        UnionpayTradePayRequest payRequest = getPayRequest(request);
        PaymentTradeResponse response = new PaymentTradeResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "预支付请求参数：{}", payRequest);
            }

            Map<String, String> rsp = doPay(payRequest);
            if (logger.isInfoEnabled()) {
                logger.info(request, "预支付响应参数：{}", rsp);
            }

            if (UnionpayConstants.SUCCESS_CODE.equals(rsp.get("respCode"))) {
                setPaySuccessResponse(response, rsp);
            } else {
                response.setErrorMsg(rsp.get("respMsg"));
                if (logger.isInfoEnabled()) {
                    logger.info(request, "预支付失败：{}", response.getErrorMsg());
                }

            }
        } catch (UnionpayException e) {
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
        Map<String, String> params = request.getParams();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "同步回调接受参数：{}", getPrettyMapForPrinter(params));
            }

            if (getUnionpayClient().verify(params)) {
                if (UnionpayConstants.SUCCESS_CODE.equals(params.get("respCode"))) {
                    response.setSuccess(true);
                    response.setOutTradeNo(params.get("orderId"));
                    response.setTradeNo(params.get("queryId"));
                    response.setAmount(CurrencyTools.ofCent(params.get("txnAmt")));
                } else {
                    response.setErrorMsg(params.get("respMsg"));
                }
                if (logger.isInfoEnabled()) {
                    logger.info(request, "同步跳转交易状态[{}]", params.get("respMsg"));
                }

            } else {
                response.setErrorMsg("同步跳转验签失败");
                if (logger.isErrorEnabled()) {
                    logger.error(request, response.getErrorMsg());
                }
            }
        } catch (UnionpayException e) {
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
        Map<String, String> params = request.getParams();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "异步回调接受参数：{}", getPrettyMapForPrinter(params));
            }

            if (getUnionpayClient().verify(params)) {
                if (UnionpayConstants.SUCCESS_CODE.equals(params.get("respCode"))) {
                    response.setSuccess(true);
                    response.setOutTradeNo(params.get("orderId"));
                    response.setTradeNo(params.get("queryId"));
                    response.setAmount(CurrencyTools.ofCent(params.get("txnAmt")));
                    response.setReplayMessage(UnionpayConstants.REPLAY_SUCCESS);
                } else {
                    response.setErrorMsg(params.get("respMsg"));
                }
                if (logger.isInfoEnabled()) {
                    logger.info(request, "异步回调交易状态[{}]", params.get("respMsg"));
                }

            } else {
                response.setErrorMsg("异步回调验签失败");
                if (logger.isErrorEnabled()) {
                    logger.error(request, response.getErrorMsg());
                }
            }
        } catch (UnionpayException e) {
            response.setErrorMsg("异步回调错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        PaymentTradeQueryResponse response = new PaymentTradeQueryResponse();
        UnionpayTradeQueryRequest queryRequest = getQueryRequest(request);
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询支付交易请求参数：{}", queryRequest);
            }

            Map<String, String> rsp = getUnionpayClient().query(queryRequest);
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询支付交易响应参数：{}", getPrettyMapForPrinter(rsp));
            }

            //如果查询交易成功
            String respCode = rsp.get("respCode");
            if (UnionpayConstants.SUCCESS_CODE.equals(respCode)) {
                //处理被查询交易的应答码逻辑
                String origRespCode = rsp.get("origRespCode");
                String respMsg = rsp.get("origRespMsg");
                // 交易成功
                if (UnionpayConstants.SUCCESS_CODE.equals(origRespCode)) {
                    response.setSuccess(true);
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setTradeNo(rsp.get("queryId"));
                    response.setAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                    respMsg = "成功";
                } else if ("03".equals(origRespCode) || "04".equals(origRespCode) || "05".equals(origRespCode)) {
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setTradeNo(rsp.get("queryId"));
                    response.setAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                    response.setErrorMsg("等待支付完成，请稍后再试");
                } else {
                    response.setErrorMsg(respMsg);
                }
                if (logger.isInfoEnabled()) {
                    logger.info(request, "查询支付交易状态[{}]", respMsg);
                }

            } else {
                // 查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                if ("34".equals(respCode)) {
                    response.setNotExist(true);
                }
                response.setErrorMsg(rsp.get("respMsg"));
                if (logger.isInfoEnabled()) {
                    logger.info(request, "查询支付交易失败：{}", response.getErrorMsg());
                }

            }
        } catch (UnionpayException e) {
            response.setErrorMsg("查询支付交易结果错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        UnionpayTradeRefundRequest refundRequest = getRefundRequest(request);
        PaymentTradeRefundResponse response = new PaymentTradeRefundResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "申请退款请求参数：{}", refundRequest);
            }

            Map<String, String> rsp = getUnionpayClient().refund(refundRequest);
            if (logger.isInfoEnabled()) {
                logger.info(request, "申请退款响应参数：{}", getPrettyMapForPrinter(rsp));
            }

            String respCode = rsp.get("respCode");
            String respMsg = rsp.get("respMsg");
            //如果查询交易成功
            if (UnionpayConstants.SUCCESS_CODE.equals(respCode)) {
                // 交易成功
                response.setSuccess(true);
                response.setOutTradeNo(request.getOutTradeNo());
                response.setOutRefundNo(request.getOutRefundNo());
                response.setTradeNo(request.getTradeNo());
                response.setRefundNo(rsp.get("queryId"));
                response.setRefundAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                response.setTotalAmount(request.getTotalAmount());
                respMsg = "成功";
            } else if ("01".equals(respCode) || "03".equals(respCode) || "05".equals(respCode)) {
                response.setErrorMsg("等待退款完成，请稍后再试");
            } else {
                // 查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                if ("34".equals(respCode)) {
                    response.setNotExist(true);
                }
                response.setErrorMsg(respMsg);
            }
            if (logger.isInfoEnabled()) {
                logger.info(request, "申请退款状态[{}]", respMsg);
            }

        } catch (UnionpayException e) {
            response.setErrorMsg("申请退款错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        UnionpayTradeRefundQueryRequest refundQueryRequest = getRefundQueryRequest(request);
        PaymentTradeRefundQueryResponse response = new PaymentTradeRefundQueryResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询退款请求参数：{}", refundQueryRequest);
            }

            Map<String, String> rsp = getUnionpayClient().refundQuery(refundQueryRequest);
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询退款响应参数：{}", getPrettyMapForPrinter(rsp));
            }

            //如果查询交易成功
            String respCode = rsp.get("respCode");
            if (UnionpayConstants.SUCCESS_CODE.equals(respCode)) {
                //处理被查询交易的应答码逻辑
                String origRespCode = rsp.get("origRespCode");
                String respMsg = rsp.get("origRespMsg");
                // 交易成功
                if (UnionpayConstants.SUCCESS_CODE.equals(origRespCode)) {
                    response.setSuccess(true);
                    response.setOutRefundNo(request.getOutRefundNo());
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setTradeNo(request.getTradeNo());
                    response.setRefundNo(rsp.get("queryId"));
                    response.setTotalAmount(CurrencyTools.ofCent(rsp.get("txnAmt")));
                    respMsg = "成功";
                } else if ("03".equals(origRespCode) || "04".equals(origRespCode) || "05".equals(origRespCode)) {
                    response.setErrorMsg("等待退款完成，请稍后再试");
                } else {
                    response.setErrorMsg(respMsg);
                }
                if (logger.isInfoEnabled()) {
                    logger.info(request, "查询退款状态[{}]", respMsg);
                }

            } else {
                // 查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                if ("34".equals(respCode)) {
                    response.setNotExist(true);
                }
                response.setErrorMsg(rsp.get("respMsg"));
                if (logger.isInfoEnabled()) {
                    logger.info(request, "查询退款失败：{}", response.getErrorMsg());
                }

            }
        } catch (UnionpayException e) {
            response.setErrorMsg("查询退款错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    protected UnionpayTradePayRequest getPayRequest(PaymentTradeRequest request) {
        UnionpayTradePayRequest payRequest = new UnionpayTradePayRequest();
        payRequest.setBizType(getBizType());
        // 交易类型 01:消费
        payRequest.setTradeType("01");
        payRequest.setOutTradeNo(request.getOutTradeNo());
        payRequest.setAmount(CurrencyTools.toCent(request.getAmount()));
        payRequest.setSubject(request.getSubject());
        payRequest.setNotifyUrl(getProperties().getNotifyUrl());
        return payRequest;
    }

    protected UnionpayTradeQueryRequest getQueryRequest(PaymentTradeQueryRequest request) {
        UnionpayTradeQueryRequest queryRequest = new UnionpayTradeQueryRequest();
        queryRequest.setBizType(getBizType());
        // 交易类型 00 查询
        queryRequest.setTradeType("00");
        queryRequest.setTradeSubType("00");
        queryRequest.setOutTradeNo(request.getOutTradeNo());
        return queryRequest;
    }

    protected UnionpayTradeRefundRequest getRefundRequest(PaymentTradeRefundRequest request) {
        UnionpayTradeRefundRequest refundRequest = new UnionpayTradeRefundRequest();
        refundRequest.setBizType(getBizType());
        // 交易类型 04 退货
        refundRequest.setTradeType("04");
        refundRequest.setOutRefundNo(request.getOutRefundNo());
        refundRequest.setTradeNo(request.getTradeNo());
        refundRequest.setRefundAmount(CurrencyTools.toCent(request.getRefundAmount()));
        return refundRequest;
    }

    protected UnionpayTradeRefundQueryRequest getRefundQueryRequest(PaymentTradeRefundQueryRequest request) {
        UnionpayTradeRefundQueryRequest refundQueryRequest = new UnionpayTradeRefundQueryRequest();
        refundQueryRequest.setBizType(getBizType());
        // 交易类型 00 查询
        refundQueryRequest.setTradeType("00");
        refundQueryRequest.setTradeSubType("00");
        refundQueryRequest.setOutRefundNo(request.getOutRefundNo());
        return refundQueryRequest;
    }

    public abstract UnionpayClient getUnionpayClient();

    public abstract void setProperties(UnionpayProperties properties);

    public abstract UnionpayProperties getProperties();

    public abstract String getBizType();

    protected Map<String, String> doPay(UnionpayTradePayRequest request) throws UnionpayException {
        throw new UnsupportedOperationException();
    }

    protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) {
        response.setSuccess(true);
    }

    protected Map<String, String> getPrettyMapForPrinter(Map<String, String> input) {
        Map<String, String> prettyMap = new HashMap<>(input);
        prettyMap.replace(UnionpayConstants.param_signPubKeyCert, "**已隐藏**");
        return prettyMap;
    }
}
