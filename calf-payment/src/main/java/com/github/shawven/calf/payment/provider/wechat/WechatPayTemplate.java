package com.github.shawven.calf.payment.provider.wechat;

import com.github.shawven.calf.payment.client.*;
import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.provider.wechat.sdk.WXPay;
import com.github.shawven.calf.payment.provider.wechat.sdk.WXPayConstants;
import com.github.shawven.calf.payment.provider.wechat.sdk.WXPayUtil;
import com.github.shawven.calf.payment.support.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.github.shawven.calf.payment.client.*;
import com.github.shawven.calf.payment.domain.*;
import com.github.shawven.calf.payment.properties.AppProperties;
import com.github.shawven.calf.payment.properties.WechatPayProperties;
import com.github.shawven.calf.payment.provider.AbstractPaymentTemplate;
import com.github.shawven.calf.payment.provider.PaymentProviderEnum;
import com.github.shawven.calf.payment.support.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.shawven.calf.payment.provider.PaymentProviderEnum.WECHAT;

/**
 * @author Shoven
 * @date 2019-09-02
 */
public abstract class WechatPayTemplate extends AbstractPaymentTemplate {

    protected WechatPayProperties properties;

    @Override
    public PaymentProviderEnum getProvider() {
        return WECHAT;
    }

    @Override
    public PaymentTradeResponse pay(PaymentTradeRequest request) {
        Map<String, String> params = getPayParams(request);
        PaymentTradeResponse response = new PaymentTradeResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "预支付请求参数：{}", params);
            }

            Map<String, String> rsp = getWechatPayClient().unifiedOrder(params);
            if (logger.isInfoEnabled()) {
                logger.info(request, "预支付响应参数：{}", rsp);
            }

            if (WXPayConstants.SUCCESS.equals(rsp.get("return_code"))) {
                if (WXPayConstants.SUCCESS.equals(rsp.get("result_code"))) {
                    setPaySuccessResponse(response, rsp);
                } else {
                    response.setErrorMsg(rsp.get("err_code_des"));
                    if (logger.isInfoEnabled()) {
                        logger.info(request, "预支付失败：{}", response.getErrorMsg());
                    }

                }
            } else {
                response.setErrorMsg("预支付失败：" + rsp.get("return_msg"));
                if (logger.isErrorEnabled()) {
                    logger.error(request, response.getErrorMsg());
                }
            }
        } catch (Exception e) {
            response.setErrorMsg("预支付错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeQueryResponse query(PaymentTradeQueryRequest request) {
        HashMap<String, String> params = new HashMap<>();
        params.put("out_trade_no", request.getOutTradeNo());

        PaymentTradeQueryResponse response = new PaymentTradeQueryResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询支付交易请求参数：{}", params);
            }

            Map<String, String> rsp = getWechatPayClient().orderQuery(params);
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询支付交易响应参数：{}", rsp);
            }

            if (WXPayConstants.SUCCESS.equals(rsp.get("return_code"))) {
                if (WXPayConstants.SUCCESS.equals(rsp.get("result_code"))) {
                    String tradeState = rsp.get("trade_state");
                    String tradeStateDesc = rsp.get("trade_state_desc");
                    if (WXPayConstants.SUCCESS.equals(tradeState)) {
                        response.setSuccess(true);
                        response.setOutTradeNo(rsp.get("out_trade_no"));
                        response.setTradeNo(rsp.get("transaction_id"));
                        response.setAmount(CurrencyTools.ofCent(rsp.get("total_fee")));
                    } else {
                        response.setErrorMsg(tradeStateDesc);
                    }
                    response.setState(tradeState);
                    if (logger.isInfoEnabled()) {
                        logger.info(request, "查询支付交易交易状态[{}]", tradeStateDesc);
                    }

                } else {
                    if (WXPayConstants.ORDER_NOT_EXIST.equals(rsp.get("err_code"))) {
                        response.setNotExist(true);
                    }
                    response.setErrorMsg(rsp.get("err_code_des"));
                    if (logger.isInfoEnabled()) {
                        logger.info(request, "查询支付交易失败：{}", response.getErrorMsg());
                    }

                }
            } else {
                response.setErrorMsg(rsp.get("return_msg"));
                if (logger.isInfoEnabled()) {
                    logger.info(request, "查询支付交易失败：{}", response.getErrorMsg());
                }

            }
        } catch (Exception e) {
            response.setErrorMsg("查询支付交易错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
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
            if (logger.isInfoEnabled()) {
                logger.info(request, "异步回调接受参数：{}", params);
            }

            if (WXPayConstants.SUCCESS.equals(params.get("result_code"))) {
                boolean validation = getWechatPayClient().isResponseSignatureValid(params);
                if (validation) {
                    if (logger.isInfoEnabled()) {
                        logger.info(request, "异步回调验签通过");
                    }

                    String tradeStatusDesc = params.get("err_code_des");
                    if (WXPayConstants.SUCCESS.equals(params.get("return_code"))) {
                        response.setSuccess(true);
                        response.setOutTradeNo(params.get("out_trade_no"));
                        response.setTradeNo(params.get("transaction_id"));
                        response.setAmount(CurrencyTools.ofCent(params.get("total_fee")));
                        replay.put("return_code", WXPayConstants.SUCCESS);
                        replay.put("return_msg", WXPayConstants.OK);
                    } else {
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
            } else {
                response.setErrorMsg(params.get("return_msg"));
                if (logger.isErrorEnabled()) {
                    logger.error(request, "异步回调失败：{}", response.getErrorMsg());
                }
            }
        } catch (Exception e) {
            response.setErrorMsg("异步回调错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
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
        params.put("out_trade_no", request.getOutTradeNo());
        params.put("out_refund_no", request.getOutRefundNo());
        params.put("total_fee", CurrencyTools.toCent(request.getTotalAmount()));
        params.put("refund_fee", CurrencyTools.toCent(request.getRefundAmount()));
        params.put("refund_desc", request.getRefundReason());

        PaymentTradeRefundResponse response = new PaymentTradeRefundResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "申请退款请求参数：{}", params);
            }

            Map<String, String> rsp = getWechatPayClient().refund(params);
            if (logger.isInfoEnabled()) {
                logger.info(request, "申请退款响应参数：{}", rsp);
            }

            if (WXPayConstants.SUCCESS.equals(rsp.get("return_code"))) {
                String desc = rsp.get("err_code_des");
                if (WXPayConstants.SUCCESS.equals(rsp.get("result_code"))) {
                    response.setSuccess(true);
                    response.setOutTradeNo(request.getOutTradeNo());
                    response.setOutRefundNo(request.getOutRefundNo());
                    response.setTradeNo(rsp.get("transaction_id"));
                    response.setRefundNo(rsp.get("refund_id"));
                    response.setRefundAmount(CurrencyTools.ofCent(rsp.get("refund_fee")));
                    response.setTotalAmount(CurrencyTools.ofCent(rsp.get("total_fee")));
                    desc = "退款成功";
                } else {
                    response.setErrorMsg(desc);
                }
                if (logger.isInfoEnabled()) {
                    logger.info(request, "申请退款状态[{}]", desc);
                }

            } else {
                if (WXPayConstants.ORDER_NOT_EXIST.equals(rsp.get("err_code"))) {
                    response.setNotExist(true);
                }
                response.setErrorMsg(rsp.get("return_msg"));
                if (logger.isErrorEnabled()) {
                    logger.error(request, "申请退款失败：{}", response.getErrorMsg());
                }
            }
        } catch (Exception e) {
            response.setErrorMsg("申请退款错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
        }
        return response;
    }

    @Override
    public PaymentTradeRefundQueryResponse refundQuery(PaymentTradeRefundQueryRequest request) {
        WXPay client = getWechatPayClient();
        HashMap<String, String> params = new HashMap<>();
        params.put("out_trade_no", request.getOutTradeNo());
        params.put("refund_id", request.getOutRefundNo());

        PaymentTradeRefundQueryResponse response = new PaymentTradeRefundQueryResponse();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询退款请求参数：{}", params);
            }

            Map<String, String> rsp = client.refundQuery(params);
            if (logger.isInfoEnabled()) {
                logger.info(request, "查询退款响应参数：{}", rsp);
            }

            if (WXPayConstants.SUCCESS.equals(rsp.get("return_code"))) {
                String refundStatus;
                if (WXPayConstants.SUCCESS.equals(rsp.get("result_code"))) {
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
                        if (WXPayConstants.REFUND_CLOSE.equals(refundStatus)) {
                            refundStatus = "退款关闭";
                        }
                        if (WXPayConstants.PROCESSING.equals(refundStatus)) {
                            refundStatus = "退款处理中";
                        }
                        if (WXPayConstants.CHANGE.equals(refundStatus)) {
                            refundStatus = "退款异常";
                        }
                        response.setErrorMsg(refundStatus);
                    }
                } else {
                    if (WXPayConstants.REFUND_NOT_EXIST.equals(rsp.get("err_code"))) {
                        response.setNotExist(true);
                    }
                    refundStatus = rsp.get("err_code_des");
                    response.setErrorMsg(refundStatus);
                }
                if (logger.isInfoEnabled()) {
                    logger.info(request, "查询退款状态[{}]", refundStatus);
                }

            } else {
                response.setErrorMsg(rsp.get("return_msg"));
                if (logger.isErrorEnabled()) {
                    logger.error(request, "查询退款失败：{}", response.getErrorMsg());
                }
            }
        } catch (Exception e) {
            response.setErrorMsg("查询退款错误：" + e.getMessage());
            if (logger.isErrorEnabled()) {
                logger.error(request, response.getErrorMsg());
            }
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

    protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) throws Exception {
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

    public static class WebQrc extends WechatPayTemplate implements WebQrcTradeClientType {

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = super.getPayParams(request);
            params.put("trade_type", WXPayConstants.TRADE_TYPE_NATIVE);
            return params;
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) {
            response.setSuccess(true);
            response.setCodeUrl(rsp.get("code_url"));
        }
    }

    public static class Wap extends WechatPayTemplate implements WapTradeClientType {

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = super.getPayParams(request);
            params.put("trade_type", WXPayConstants.TRADE_TYPE_H5);
            // 场景信息
            params.put("scene_info", getSceneInfo());
            return params;
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) {
            response.setSuccess(true);
            response.setUrl(rsp.get("mweb_url"));
        }

        protected String getSceneInfo() {
            AppProperties appProperties = getAppProperties();
            return new StringBuilder("{")
                    .append("\"h5_info\":{")
                    .append("\"type\":\"").append("Wap").append("\"")
                    .append("\"wap_url\":\"").append(appProperties.getServerDomain()).append("\"")
                    .append("\"wap_name\":\"").append(appProperties.getAppName()).append("\"")
                    .append("}}")
                    .toString();
        }
    }

    public static class App extends WechatPayTemplate implements AppTradeClientType {

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = super.getPayParams(request);
            params.put("trade_type", WXPayConstants.TRADE_TYPE_APP);
            return params;
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) throws Exception {
            response.setSuccess(true);
            response.setBody(getWechatPayClient().prepareAppPay(rsp.get("prepay_id")));
        }
    }

    public static class JsApi extends WechatPayTemplate implements JsApiTradeClientType {

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            Map<String, String> params = super.getPayParams(request);
            params.put("trade_type", WXPayConstants.TRADE_TYPE_JSAPI);
            params.put("openid", getOpenId(request));
            return params;
        }

        @Override
        protected void setPaySuccessResponse(PaymentTradeResponse response, Map<String, String> rsp) throws Exception {
            response.setSuccess(true);
            response.setBody(getWechatPayClient().prepareJsApiPay(rsp.get("prepay_id")));
        }

        public String getOpenId(PaymentTradeRequest request) {
            String openId = null;
            try {
                Map<String, String> query = new HashMap<>();
                query.put("appid", properties.getAppId());
                query.put("secret", properties.getAppSecret());
                query.put("code", request.get(PaymentConstants.CODE));
                query.put("grant_type", "authorization_code");

                if (logger.isInfoEnabled()) {
                    logger.info(request, "获取openid请求：{}", query);
                }

                HttpUtil httpUtil = PaymentContextHolder.getHttp();
                String s = httpUtil.get("https://api.weixin.qq.com/sns/oauth2/access_token", query);
                Map<String, String> rsp = new Gson().fromJson(s, new TypeToken<Map<String, String>>() {
                }.getType());
                if (logger.isInfoEnabled()) {
                    logger.info(request, "获取openid响应：{}", rsp);
                }

                if (rsp.get("errcode") != null) {
                    if (logger.isErrorEnabled()) {
                        logger.error(request, "获取openId失败:" + rsp.get("errmsg"));
                    }
                    throw new RuntimeException("出现错误无法进行支付,请更换其他支付方式");
                } else {
                    openId = rsp.get("openid");
                }
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(request, "获取openId出错");
                }
            }
            return openId;
        }
    }

    public static class F2f extends WechatPayTemplate implements F2fTradeClientType {

        @Override
        public PaymentTradeResponse pay(PaymentTradeRequest request) {
            Map<String, String> params = getPayParams(request);
            PaymentTradeResponse response = new PaymentTradeResponse();
            boolean needQuery = false;

            try {
                if (logger.isInfoEnabled()) {
                    logger.info(request, "支付请求参数：{}", params);
                }

                Map<String, String> rsp = getWechatPayClient().microPay(params);
                if (logger.isInfoEnabled()) {
                    logger.info(request, "支付响应参数：{}", rsp);
                }

                if (WXPayConstants.SUCCESS.equals(rsp.get("return_code"))) {
                    String errCode = rsp.get("err_code");
                    if (WXPayConstants.SUCCESS.equals(rsp.get("result_code"))) {
                        response.setSuccess(true);
                        response.setTradeSuccess(true);
                        response.setTradeNo(rsp.get("transaction_id"));
                        response.setOutTradeNo(rsp.get("out_trade_no"));
                    } else if (WXPayConstants.SYSTEM_ERROR.equals(errCode)
                            || WXPayConstants.USER_PAYING.equals(errCode)) {
                        needQuery = true;
                        response.setErrorMsg(rsp.get("err_code_des"));
                    } else {
                        response.setErrorMsg("支付失败:" + rsp.get("err_code_des"));
                    }
                } else {
                    response.setErrorMsg("支付失败：" + rsp.get("return_msg"));
                }
            } catch (Exception e) {
                if (e instanceof IOException || e.getCause() instanceof IOException) {
                    needQuery = true;
                }
                response.setErrorMsg("支付错误：" + e.getMessage());
            }
            if (!response.isSuccess()) {
                if (needQuery) {
                    if (logger.isInfoEnabled()) {
                        logger.info(request, "支付未完成[{}]，正在轮训查询订单", response.getErrorMsg());
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

        protected void queryTradeIfFailed(PaymentTradeRequest request, PaymentTradeResponse response) {
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
            if (queryResponse.isSuccess()) {
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
            Map<String, String> params = getPayParams(request);
            params.put("out_trade_no", request.getOutTradeNo());
            try {
                if (logger.isInfoEnabled()) {
                    logger.info(request, "取消订单请求参数：{}", params);
                }

                Map<String, String> rsp = getWechatPayClient().reverse(params);
                if (logger.isInfoEnabled()) {
                    logger.info(request, "取消订单响应参数：{}", rsp);
                }

                if (WXPayConstants.SUCCESS.equals(rsp.get("return_code"))) {
                    if (!WXPayConstants.SUCCESS.equals(rsp.get("result_code"))) {
                        if (logger.isInfoEnabled()) {
                            logger.info(request, "取消订单失败：{}", rsp.get("err_code_des"));
                        }

                        return false;
                    }
                    if ("Y".equals(rsp.get("recall"))) {
                        if (logger.isErrorEnabled()) {
                            logger.error(request, "取消订单失败需要业务重试");
                        }
                        return false;
                    }
                    return true;
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info(request, "取消订单失败：{}", rsp.get("return_msg"));
                    }

                    return false;
                }
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(request, "取消订单错误：{}", e.getMessage());
                }
                return true;
            }
        }

        @Override
        protected Map<String, String> getPayParams(PaymentTradeRequest request) {
            String authCode = request.get(PaymentConstants.AUTH_CODE);
            if (PaymentUtils.isBlankString(authCode)) {
                throw new IllegalArgumentException("支付授权码不能为空");
            }
            Map<String, String> params = super.getPayParams(request);
            params.put("auth_code", authCode);
            return params;
        }

    }
}
