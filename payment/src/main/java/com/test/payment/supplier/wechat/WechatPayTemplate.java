package com.test.payment.supplier.wechat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.payment.client.*;
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
import java.util.concurrent.atomic.AtomicReference;

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
        HashMap<String, String> params = new HashMap<>();
        params.put("transaction_id", request.getTradeNo());
        params.put("out_trade_no", request.getOutTradeNo());

        PaymentTradeQueryResponse response = new PaymentTradeQueryResponse();
        try {
            logger.info(request, "查询支付交易请求参数：{}", params);
            Map<String, String> rsp = getWechatPayClient().orderQuery(params);
            logger.info(request, "查询支付交易响应参数：{}", rsp);

            String returnCode = rsp.get("return_code");
            if (WXPayConstants.SUCCESS.equals(returnCode)) {
                String resultCode = rsp.get("result_code");
                if (WXPayConstants.SUCCESS.equals(resultCode)) {
                    String tradeState = rsp.get("trade_state");
                    String tradeStateDesc = rsp.get("trade_state_desc");
                    if (WXPayConstants.SUCCESS.equals(tradeState)) {
                        response.setSuccess(true);
                        response.setOutTradeNo(rsp.get("out_trade_no"));
                        response.setTradeNo(request.getOutTradeNo());
                        response.setAmount(CurrencyTools.ofCent(rsp.get("total_fee")));
                    } else {
                        response.setErrorMsg(tradeStateDesc);
                    }
                    response.setState(tradeState);
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
    public PaymentTradeCallbackResponse asyncNotify(PaymentTradeCallbackRequest request) {
        PaymentTradeCallbackResponse response = new PaymentTradeCallbackResponse();
        HashMap<String, String> replay = new HashMap<>(2);
        replay.put("return_code", WXPayConstants.FAIL);
        replay.put("return_msg", WXPayConstants.FAIL);

        try {
            if (PaymentUtils.isBlankString(request.getRowBody())) {
                try {
                    String s = WXPayUtil.mapToXml(replay);
                    response.setReplayMessage(s);
                } catch (Exception ignored) {
                }
                return response;
            }

            Map<String, String> params = WXPayUtil.xmlToMap(request.getRowBody());
            logger.info(request, "异步回调接受参数：{}", params);

            if (WXPayConstants.SUCCESS.equals(params.get("result_code"))) {
                boolean validation = getWechatPayClient().isResponseSignatureValid(params);
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
                        replay.put("return_code", WXPayConstants.SUCCESS);
                        replay.put("return_msg", WXPayConstants.OK);
                    } else {
                        response.setErrorMsg(tradeStatusDesc);
                    }
                    logger.info(request, "异步回调交易状态[{}]", tradeStatusDesc);
                } else {
                    response.setErrorMsg("异步回调验签失败");
                    logger.error(request, "异步回调验签失败");
                }
            } else {
                String errorMsg = params.get("return_msg");
                response.setErrorMsg(errorMsg);
                logger.error(request, "异步回调失败：{}", errorMsg);
            }
        } catch (Exception e) {
            response.setErrorMsg("异步回调错误：" + e.getMessage());
            logger.error(request, "异步回调错误：{}", e.getMessage());
        }

        try {
            String s = WXPayUtil.mapToXml(replay);
            response.setReplayMessage(s);
        } catch (Exception ignored) {
        }
        return response;
    }

    @Override
    public PaymentTradeRefundResponse refund(PaymentTradeRefundRequest request) {
        HashMap<String, String> params = new HashMap<>();
        params.put("transaction_id", request.getTradeNo());
        params.put("total_fee", CurrencyTools.toCent(request.getRefundAmount()));
        params.put("refund_fee", CurrencyTools.toCent(request.getRefundAmount()));
        params.put("refund_desc", request.getRefundReason());

        PaymentTradeRefundResponse response = new PaymentTradeRefundResponse();
        try {
            logger.info(request, "申请退款请求参数：{}", params);
            Map<String, String> rsp = getWechatPayClient().refund(params);
            logger.info(request, "申请退款响应参数：{}", rsp);

            String returnCode = rsp.get("return_code");
            if (WXPayConstants.SUCCESS.equals(returnCode)) {
                String resultCode = rsp.get("result_code");
                String desc;
                if (WXPayConstants.SUCCESS.equals(resultCode)) {
                    response.setSuccess(true);
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setOutRefundNo(request.getOutRefundNo());
                    response.setTradeNo(rsp.get("transaction_id"));
                    response.setRefundNo(rsp.get("refund_id"));
                    response.setRefundAmount(CurrencyTools.ofCent(rsp.get("refund_fee")));
                    response.setTotalAmount(CurrencyTools.ofCent(rsp.get("total_fee")));
                    desc = "成功";
                } else {
                    desc = rsp.get("err_code_des");
                    response.setErrorMsg(desc);
                }
                logger.info(request, "申请退款状态[{}]", desc);
            } else {
                String errorMsg = rsp.get("return_msg");
                response.setErrorMsg(errorMsg);
                logger.error(request, "申请退款请求失败：{}", errorMsg);
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
        params.put("refund_id", request.getOutRefundNo());

        PaymentTradeRefundQueryResponse response = new PaymentTradeRefundQueryResponse();
        try {
            logger.info(request, "查询退款请求参数：{}", params);
            Map<String, String> rsp = client.refundQuery(params);
            logger.info(request, "查询退款响应参数：{}", rsp);

            String returnCode = rsp.get("return_code");
            if (WXPayConstants.SUCCESS.equals(returnCode)) {
                String resultCode = rsp.get("result_code");
                String refundStatus;
                if (WXPayConstants.SUCCESS.equals(resultCode)) {
                    refundStatus = rsp.get("refund_status_0");
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
                } else {
                    refundStatus = rsp.get("err_code_des");
                    response.setErrorMsg(refundStatus);
                }
                logger.info(request, "查询退款状态[{}]", refundStatus);
            } else {
                String errorMsg = rsp.get("return_msg");
                response.setErrorMsg(errorMsg);
                logger.error(request, "查询退款请求失败：{}", errorMsg);
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

    public abstract static class AbstractPay extends WechatPayTemplate {
        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            Map<String, String> params = getPayParams(request);
            PaymentTradeResponse response = new PaymentTradeResponse();
            try {
                logger.info(request, "预支付请求参数：{}", params);
                Map<String, String> rsp = doPay(params);
                logger.info(request, "预支付响应参数：{}", rsp);

                String returnCode = rsp.get("return_code");
                if (WXPayConstants.SUCCESS.equals(returnCode)) {
                    String resultCode = rsp.get("result_code");
                    if (WXPayConstants.SUCCESS.equals(resultCode)) {
                        setSuccessResponse(response, rsp);
                    } else {
                        String errorMsg = rsp.get("err_code_des");
                        response.setErrorMsg(errorMsg);
                        logger.info(request, "预支付失败：{}", errorMsg);
                    }
                } else {
                    String errorMsg = rsp.get("return_msg");
                    response.setErrorMsg("预支付请求失败：" + errorMsg);
                    logger.error(request, "预支付请求失败：{}", errorMsg);
                }
            } catch (Exception e) {
                response.setErrorMsg("预支付错误：" + e.getMessage());
                logger.error(request, "预支付错误：{}", e.getMessage());
            }
            return response;
        }

        protected Map<String, String> doPay(Map<String, String> params) throws Exception {
            return getWechatPayClient().unifiedOrder(params);
        }

        protected void setSuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) throws Exception {
            response.setSuccess(true);
        }

        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = new HashMap<>();
            params.put("body", request.getSubject());
            params.put("detail", request.getBody());
            params.put("out_trade_no", request.getOutTradeNo());
            params.put("total_fee", CurrencyTools.toCent(request.getAmount()));
            params.put("spbill_create_ip", request.getIp());
            return params;
        }
    }

    public static class WebQrc extends AbstractPay implements WebQrcTradeClientType {

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = super.getPayParams(request);
            params.put("trade_type", WXPayConstants.TRADE_TYPE_NATIVE);
            return params;
        }

        @Override
        protected void setSuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) {
            response.setSuccess(true);
            response.setCodeUrl(rsp.get("code_url"));
        }
    }

    public static class Wap extends AbstractPay implements WapTradeClientType {

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = super.getPayParams(request);
            params.put("trade_type", WXPayConstants.TRADE_TYPE_H5);
            // 场景信息
            params.put("scene_info", getSceneInfo());
            return params;
        }

        @Override
        protected void setSuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) {
            response.setSuccess(true);
            response.setUrl(rsp.get("mweb_url"));
        }

        protected String getSceneInfo() {
            return new StringBuilder("{")
                    .append("\"h5_info\":{")
                    .append("\"type\":\"").append("Wap").append("\"")
                    .append("\"wap_url\":\"").append(getGlobalProperties().getServerDomain()).append("\"")
                    .append("\"wap_name\":\"").append(getGlobalProperties().getAppName()).append("\"")
                    .append("}}")
                    .toString();
        }
    }

    public static class App extends AbstractPay implements AppTradeClientType {

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = super.getPayParams(request);
            params.put("trade_type", WXPayConstants.TRADE_TYPE_APP);
            return params;
        }

        @Override
        protected void setSuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) throws Exception {
            response.setSuccess(true);
            response.setBody(getWechatPayClient().prepareAppPay(rsp.get("prepay_id")));
        }
    }

    public static class F2f extends AbstractPay implements F2fTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            Map<String, String> params = getPayParams(request);
            PaymentTradeResponse response = new PaymentTradeResponse();
            try {
                logger.info(request, "支付请求参数：{}", params);
                Map<String, String> rsp = getWechatPayClient().microPay(params);
                logger.info(request, "支付响应参数：{}", rsp);

                String returnCode = rsp.get("return_code");
                if (WXPayConstants.SUCCESS.equals(returnCode)) {
                    String resultCode = rsp.get("result_code");
                    if (WXPayConstants.SUCCESS.equals(resultCode)) {
                        response.setSuccess(true);
                        response.setTradeSuccess(true);
                        response.setTradeNo(rsp.get("transaction_id"));
                        response.setOutTradeNo(rsp.get("out_trade_no"));
                    } else {
                        String errorMsg = rsp.get("err_code_des");
                        response.setErrorMsg(errorMsg);
                        logger.info(request, "预支付失败：{}", errorMsg);
                    }
                } else {
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
                }
            } catch (Exception e) {
                response.setErrorMsg("支付错误：" + e.getMessage());
                logger.error(request, "支付错误：{}", e.getMessage());
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
            Map<String, String> params = getPayParams(request);
            params.put("out_trade_no", request.getOutTradeNo());
            try {
                logger.info(request, "取消订单请求参数：{}", params);
                Map<String, String> rsp = getWechatPayClient().reverse(params);
                logger.info(request, "取消订单响应参数：{}", rsp);

                String returnCode = rsp.get("return_code");
                if (WXPayConstants.SUCCESS.equals(returnCode)) {
                    String resultCode = rsp.get("result_code");
                    if (!WXPayConstants.SUCCESS.equals(resultCode)) {
                        logger.info(request, "取消订单失败：{}", rsp.get("err_code_des"));
                        return false;
                    }
                    if ("Y".equals(rsp.get("recall"))) {
                        logger.error(request, "取消订单失败需要重试请求");
                        return false;
                    }
                    return true;
                } else {
                    logger.info(request, "取消订单请求失败：{}", rsp.get("return_msg"));
                    return false;
                }
            } catch (Exception e) {
                logger.error(request, "取消订单错误：{}", e.getMessage());
                return true;
            }
        }

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            String authCode = request.get("authCode");
            if (PaymentUtils.isBlankString(authCode)) {
                throw new IllegalArgumentException("支付授权码为空");
            }
            Map<String, String> params = super.getPayParams(request);
            params.put("auth_code", authCode);
            return params;
        }

    }

    public static class JsApi extends AbstractPay implements JsApiTradeClientType {

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = super.getPayParams(request);
            params.put("trade_type", WXPayConstants.TRADE_TYPE_JSAPI);
            params.put("openid", getOpenId(request));
            return params;
        }

        @Override
        protected void setSuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) throws Exception {
            response.setSuccess(true);
            response.setBody(getWechatPayClient().prepareJsApiPay(rsp.get("prepay_id")));
        }

        public String getOpenId(PaymentTradeRequest request) {
            String openId = null;
            try {
                Map<String, String> query = new HashMap<>();
                query.put("appid", properties.getAppId());
                query.put("secret", properties.getAppSecret());
                query.put("code", request.get("code"));
                query.put("grant_type", "authorization_code");

                logger.info(request, "获取openid请求：{}", query);
                HttpUtil httpUtil = PaymentContextHolder.getHttp();
                String s = httpUtil.get("https://api.weixin.qq.com/sns/oauth2/access_token", query);
                Map<String, String> rsp = new Gson().fromJson(s, new TypeToken<Map<String, String>>() {
                }.getType());
                logger.info(request, "获取openid响应：{}", rsp);
                if (rsp.get("errcode") != null) {
                    logger.error(request, "获取openId失败:" + rsp.get("errmsg"));
                    throw new RuntimeException("出现错误无法进行支付,请更换其他支付方式");
                } else {
                    openId = rsp.get("openid");
                }
            } catch (IOException e) {
                logger.error(request, "获取openId出错");
            }
            return openId;
        }
    }
}
