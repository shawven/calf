package com.test.payment.supplier.wechat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.payment.client.PaymentClientTypeEnum;
import com.test.payment.client.QrcTradeClientType;
import com.test.payment.client.WapTradeClientType;
import com.test.payment.domain.*;
import com.test.payment.properties.WechatPayProperties;
import com.test.payment.supplier.AbstractPaymentTemplate;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.wechat.sdk.WXPay;
import com.test.payment.supplier.wechat.sdk.WXPayConstants;
import com.test.payment.supplier.wechat.sdk.WXPayUtil;
import com.test.payment.support.CurrencyTools;
import com.test.payment.support.HttpUtil;
import com.test.payment.support.PaymentContextHolder;
import com.test.payment.support.PaymentUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.test.payment.supplier.PaymentSupplierEnum.WECHAT;

/**
 * @author Shoven
 * @date 2019-09-02
 */
public abstract class WechatPayTemplate extends AbstractPaymentTemplate {

    protected WechatPayProperties properties;

    @Override
    public PaymentSupplierEnum getSupplier() {
        return WECHAT;
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
                    logger.info(request, "查询支付交易失败：{}", errorMsg);
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
        WXPay client = getWechatPayClient();
        HashMap<String, String> params = new HashMap<>();
        params.put("out_trade_no", request.getOutTradeNo());
        params.put("out_refund_no", request.getOutRefundNo());
        params.put("total_fee", CurrencyTools.toCent(request.getRefundAmount()));
        params.put("refund_fee", CurrencyTools.toCent(request.getRefundAmount()));
        params.put("refund_desc", request.getRefundReason());

        PaymentTradeRefundResponse response = new PaymentTradeRefundResponse();
        try {
            logger.info(request, "申请退款请求参数：{}", params);
            Map<String, String> rsp = client.refund(params);
            logger.info(request, "申请退款响应参数：{}", rsp);

            String returnCode = rsp.get("return_code");
            if (WXPayConstants.SUCCESS.equals(returnCode)) {
                String resultCode = rsp.get("result_code");
                if (WXPayConstants.SUCCESS.equals(resultCode)) {
                    response.setSuccess(true);
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setOutRefundNo(request.getOutRefundNo());
                    response.setTradeNo(rsp.get("transaction_id"));
                    response.setRefundNo(rsp.get("refund_id"));
                    response.setRefundAmount(CurrencyTools.ofCent(rsp.get("refund_fee")));
                    response.setTotalAmount(CurrencyTools.ofCent(rsp.get("total_fee")));
                } else {
                    String errorMsg = rsp.get("err_code_des");
                    response.setErrorMsg(errorMsg);
                    logger.info(request, "申请退款失败：{}", errorMsg);
                }
            } else {
                String errorMsg = rsp.get("return_msg");
                response.setErrorMsg(errorMsg);
                logger.info(request, "申请退款请求失败：{}", errorMsg);
            }
        } catch (Exception e) {
            response.setErrorMsg("申请退款错误：" + e.getMessage());
            logger.error(request, "申请退款错误：{}", e.getMessage());
        }
        return response;
    }

    @Override
        public PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        WXPay client = getWechatPayClient();
        HashMap<String, String> params = new HashMap<>();
        params.put("out_refund_no", request.getOutRefundNo());
        params.put("out_trade_no", request.getOutTradeNo());

        PaymentTradeRefundQueryResponse response = new PaymentTradeRefundQueryResponse();
        try {
            logger.info(request, "查询退款请求参数：{}", params);
            Map<String, String> rsp = client.refundQuery(params);
            logger.info(request, "查询退款响应参数：{}", rsp);

            String returnCode = rsp.get("return_code");
            if (WXPayConstants.SUCCESS.equals(returnCode)) {
                String resultCode = rsp.get("result_code");
                if (WXPayConstants.SUCCESS.equals(resultCode)) {
                    String refundStatus = rsp.get("refund_status_0");
                    if (WXPayConstants.SUCCESS.equals(refundStatus)) {
                        response.setSuccess(true);
                        response.setOutTradeNo(request.getOutTradeNo());
                        response.setOutRefundNo(request.getOutRefundNo());
                        response.setTradeNo(rsp.get("transaction_id"));
                        response.setRefundNo(rsp.get("refund_id_0"));
                        response.setTotalAmount(rsp.get("total_fee"));
                        response.setRefundAmount(rsp.get("refund_fee_0"));
                    } else {
                        if ("REFUNDCLOSE".equals(refundStatus)) {
                            refundStatus = "退款关闭";
                        }
                        if ("PROCESSING".equals(refundStatus)) {
                            refundStatus = "退款处理中";
                        }
                        if ("CHANGE".equals(refundStatus)) {
                            refundStatus = "退款异常";
                        }
                        response.setErrorMsg(refundStatus);
                    }
                    logger.info(request, "查询退款状态[{}]", refundStatus);
                } else {
                    String errorMsg = rsp.get("err_code_des");
                    response.setErrorMsg(errorMsg);
                    logger.info(request, "查询退款失败：{}", errorMsg);
                }
            } else {
                String errorMsg = rsp.get("return_msg");
                response.setErrorMsg(errorMsg);
                logger.info(request, "查询退款请求失败：{}", errorMsg);
            }
        } catch (Exception e) {
            response.setErrorMsg("查询退款错误：" + e.getMessage());
            logger.error(request, "查询退款错误：{}", e.getMessage());
        }
        return response;
    }

    public WXPay getWechatPayClient() {
        return WechatPayClientFactory.getInstance(properties);
    }

    public WechatPayProperties getProperties() {
        return properties;
    }

    public void setProperties(WechatPayProperties properties) {
        this.properties = properties;
    }

    public static class Qrc extends WechatPayTemplate implements QrcTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            WXPay client = getWechatPayClient();
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
                Map<String, String> rsp = client.unifiedOrder(params);
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
            WXPay client = getWechatPayClient();
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
                Map<String, String> rsp = client.unifiedOrder(params);
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

    public static class JsApi extends WechatPayTemplate {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            WXPay client = getWechatPayClient();
            Map<String, String> params = new HashMap<>();
            params.put("body", request.getSubject());
            params.put("detail", request.getBody());
            params.put("out_trade_no", request.getOutTradeNo());
            params.put("total_fee", CurrencyTools.toCent(request.getAmount()));
            params.put("spbill_create_ip", request.getIp());
            params.put("trade_type", "JSAPI");
            params.put("openId", getOpenId(request));

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
                Map<String, String> rsp = client.unifiedOrder(params);
                logger.info(request, "预支付响应参数：{}", rsp);

                String returnCode = rsp.get("return_code");
                if (WXPayConstants.SUCCESS.equals(returnCode)) {
                    String resultCode = rsp.get("result_code");
                    if (WXPayConstants.SUCCESS.equals(resultCode)) {
                        response.setSuccess(true);
                        response.setBody(getWechatPayClient().prepareJsApiPay(rsp.get("prepay_id")));
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

        public String getOpenId(PaymentTradeRequest request) {
            String code = request.get("code");
            if (PaymentUtils.isBlankString(code)) {
                throw new RuntimeException("缺少code参数");
            }
            String openId = null;
            try {
                Map<String, String> query = new HashMap<>();
                query.put("appid", "wx6b9ebd5d4e720f5f");
                query.put("secret", "4c00fd00d8420c4023619bca25bb175e");
                query.put("code", request.get("code"));
                query.put("grant_type", "authorization_code");

                logger.info(request, "获取openid请求：{}", query);
                HttpUtil httpUtil = PaymentContextHolder.getHttp();
                String s = httpUtil.get("https://api.weixin.qq.com/sns/oauth2/access_token", query);
                Map<String, String> rsp = new Gson().fromJson(s, new TypeToken<Map<String, String>>(){}.getType());
                logger.info(request, "获取openid响应：{}", rsp);
                if (rsp.get("errcode") != null) {
                    throw new RuntimeException("获取openId失败:" + rsp.get("errmsg"));
                } else {
                    openId = rsp.get("openid");
                }
            } catch (IOException e) {
                logger.error(request, "获取openId出错");
            }
            return openId;
        }

        @Override
        public PaymentClientTypeEnum getClientType() {
            return PaymentClientTypeEnum.JSAPI;
        }
    }
}
