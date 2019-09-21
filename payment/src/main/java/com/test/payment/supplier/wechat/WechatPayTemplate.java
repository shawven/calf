package com.test.payment.supplier.wechat;

import com.test.payment.client.QrcTradeClientType;
import com.test.payment.client.WapTradeClientType;
import com.test.payment.client.WebTradeClientType;
import com.test.payment.domain.*;
import com.test.payment.properties.PaymentProperties;
import com.test.payment.properties.WechatPayProperties;
import com.test.payment.supplier.AbstractPaymentTemplate;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.wechat.sdk.WXPay;
import com.test.payment.supplier.wechat.sdk.WXPayConstants;
import com.test.payment.supplier.wechat.sdk.WXPayUtil;
import com.test.payment.support.CurrencyTools;
import com.test.payment.support.PaymentUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-09-02
 */
public abstract class WechatPayTemplate  extends AbstractPaymentTemplate {

    protected WechatPayProperties properties;

    @Override
    public PaymentSupplierEnum getSupplier() {
        return PaymentSupplierEnum.WECHAT;
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        WXPay client = getWechatPayClient();
        HashMap<String, String> params = new HashMap<>();
        params.put("out_trade_no", request.getOutTradeNo());

        PaymentTradeQueryResponse response = new PaymentTradeQueryResponse();
        try {
            logger.info(request, "查询支付交易请求参数：{}", params);
            Map<String, String> rsp = client.orderQuery(params);
            logger.info(request, "查询支付交易响应参数：{}", rsp);

            String returnCode = rsp.get("return_code");
            if (WXPayConstants.SUCCESS.equals(returnCode)) {
                String resultCode = rsp.get("result_code");
                if (WXPayConstants.SUCCESS.equals(resultCode)) {
                    String tradeState = rsp.get("trade_state");
                    String tradeStateDesc = rsp.get("trade_state_desc");
                    if (WXPayConstants.SUCCESS.equals(tradeState)) {
                        response.setSuccess(true);
                        response.setOutTradeNo(request.getOutTradeNo());
                        response.setTradeNo(rsp.get("transaction_id"));
                        response.setAmount(CurrencyTools.ofCent(rsp.get("total_fee")));
                    } else {
                        response.setErrorMsg(tradeStateDesc);
                    }
                    logger.info(request, "查询支付交易交易状态[{}]", tradeStateDesc);
                } else {
                    String errorMsg = rsp.get("err_code_des");
                    response.setErrorMsg(errorMsg);
                    logger.info(request, "查询支付交易错误：{}", errorMsg);
                }
            } else {
                String errorMsg = rsp.get("return_msg");
                response.setErrorMsg(errorMsg);
                logger.info(request, "查询支付交易请求失败：{}", errorMsg);
            }
        } catch (Exception e) {
            response.setErrorMsg("查询支付交易错误：" + e.getMessage());
            logger.error(request, "查询支付交易错误：{}", e.getMessage());
        }
        return response;
    }


    @Override
    public PaymentTradeCallbackResponse syncReturn(PaymentTradeCallbackRequest request) {
        return null;
    }

    @Override
    public PaymentTradeCallbackResponse asyncNotify(PaymentTradeCallbackRequest request) {
        WXPay client = getWechatPayClient();
        PaymentTradeCallbackResponse response = new PaymentTradeCallbackResponse();
        HashMap<String, String> replay = new HashMap<>(2);
        replay.put("return_code", WXPayConstants.FAIL);
        replay.put("return_msg", WXPayConstants.FAIL);

        try {
            if (PaymentUtils.isBlankString(request.getRowBody())) {
                try {
                    String s = WXPayUtil.mapToXml(replay);
                    response.setReplayMessage(s);
                } catch (Exception ignored) { }
                return response;
            }

            Map<String, String> params = WXPayUtil.xmlToMap(request.getRowBody());
            logger.info(request, "异步回调接受参数：{}", params);

            if (WXPayConstants.SUCCESS.equals(params.get("result_code"))) {
                boolean validation = client.isResponseSignatureValid(params);
                if (validation) {
                    logger.info(request, "异步回调验签通过");
                    String tradeNo = params.get("transaction_id");
                    String outTradeNo = params.get("out_trade_no");
                    String totalAmount = params.get("total_fee");

                    String tradeStatusDesc = params.get("err_code_des");
                    if (WXPayConstants.SUCCESS.equals(params.get("return_code"))) {
                        response.setSuccess(true);
                        response.setOutTradeNo(outTradeNo);
                        response.setTradeNo(tradeNo);
                        response.setAmount(CurrencyTools.ofCent(totalAmount));
                        replay.put("return_code", "SUCCESS");
                        replay.put("return_msg", "OK");
                    } else {
                        response.setErrorMsg(tradeStatusDesc);
                    }
                    logger.info(request, "异步回调交易状态[{}]", tradeStatusDesc);
                } else {
                    response.setErrorMsg("异步回调验签失败");
                    logger.info(request, "异步回调验签失败");
                }
            } else {
                String errorMsg = params.get("return_msg");
                response.setErrorMsg(errorMsg);
                logger.info(request, "异步回调失败：{}", errorMsg);
            }
        } catch (Exception e) {
            response.setErrorMsg("异步回调错误：" + e.getMessage());
            logger.error(request, "异步回调错误：{}", e.getMessage());
        }

        try {
            String s = WXPayUtil.mapToXml(replay);
            response.setReplayMessage(s);
        } catch (Exception ignored) { }
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

    public WXPay getWechatPayClient() {
        return WechatPayClientFactory.getInstance(properties);
    }

    public WXPay getWechatPaySlaveClient() {
        return WechatPayClientFactory.getInstance2(properties);
    }

    public WechatPayProperties getProperties() {
        return properties;
    }

    public void setProperties(WechatPayProperties properties) {
        this.properties = properties;
    }

    protected Map<String, String> retryablePay(Map<String, String> params) throws Exception {
        try {
            return getWechatPayClient().unifiedOrder(params);
        } catch (Exception e) {
            if (!(e instanceof IOException)) {
                throw e;
            }
        }
        return getWechatPaySlaveClient().unifiedOrder(params);
    }

    public static class Qrc extends WechatPayTemplate implements QrcTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            Map<String, String> params = new HashMap<>();
            params.put("body", request.getSubject());
            params.put("detail", request.getBody());
            params.put("out_trade_no", request.getOutTradeNo());
            params.put("total_fee", CurrencyTools.toCent(request.getAmount()));
            params.put("spbill_create_ip", request.getIp());
            params.put("trade_type", "NATIVE");

            PaymentTradeResponse response = new PaymentTradeResponse();
            try {
                logger.info(request, "预支付请求参数：{}", params);
                Map<String, String> rsp = retryablePay(params);
                logger.info(request, "预支付响应参数：{}", rsp);

                String returnCode = rsp.get("return_code");
                if (WXPayConstants.SUCCESS.equals(returnCode)) {
                    String resultCode = rsp.get("result_code");
                    if (WXPayConstants.SUCCESS.equals(resultCode)) {
                        response.setSuccess(true);
                        response.putBody("codeUrl", rsp.get("code_url"));
                    } else {
                        String errorMsg = rsp.get("err_code_des");
                        response.setErrorMsg(errorMsg);
                        logger.info(request, "预支付失败：{}", errorMsg);
                    }
                } else {
                    String errorMsg = rsp.get("return_msg");
                    response.setErrorMsg("预支付请求失败：" + errorMsg);
                    logger.info(request, "预支付请求失败：{}", errorMsg);
                }
            } catch (Exception e) {
                response.setErrorMsg("预支付错误：" + e.getMessage());
                logger.error(request, "预支付错误：{}", e.getMessage());
            }
            return response;
        }
    }

    public static class Wap extends WechatPayTemplate implements WapTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            Map<String, String> params = new HashMap<>();
            params.put("body", request.getSubject());
            params.put("detail", request.getBody());
            params.put("out_trade_no", request.getOutTradeNo());
            params.put("total_fee", CurrencyTools.toCent(request.getAmount()));
            params.put("spbill_create_ip", request.getIp());
            params.put("trade_type", "MWEB");

            // 场景信息
            StringBuilder sb = new StringBuilder("{")
                .append("\"h5_info\":{")
                    .append("\"type\":\"").append("Wap").append("\"")
                    .append("\"wap_url\":\"").append(getPaymentProperties().getServerDomain()).append("\"")
                    .append("\"wap_name\":\"").append(getPaymentProperties().getAppName()).append("\"")
                .append("}}");
            params.put("scene_info", sb.toString());

            PaymentTradeResponse response = new PaymentTradeResponse();
            try {
                logger.info(request, "预支付请求参数：{}", params);
                Map<String, String> rsp = retryablePay(params);
                logger.info(request, "预支付响应参数：{}", rsp);

                String returnCode = rsp.get("return_code");
                if (WXPayConstants.SUCCESS.equals(returnCode)) {
                    String resultCode = rsp.get("result_code");
                    if (WXPayConstants.SUCCESS.equals(resultCode)) {
                        response.setSuccess(true);
                        response.putBody("url", rsp.get("mweb_url"));
                    } else {
                        String errorMsg = rsp.get("err_code_des");
                        response.setErrorMsg(errorMsg);
                        logger.info(request, "预支付失败：{}", errorMsg);
                    }
                } else {
                    String errorMsg = rsp.get("return_msg");
                    response.setErrorMsg("预支付请求失败：" + errorMsg);
                    logger.info(request, "预支付请求失败：{}", errorMsg);
                }
            } catch (Exception e) {
                response.setErrorMsg("预支付错误：" + e.getMessage());
                logger.error(request, "预支付错误：{}", e.getMessage());
            }
            return response;
        }
    }
}
