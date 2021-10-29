package com.github.shawven.calf.payment.provider.unionpay.sdk;

import com.github.shawven.calf.payment.provider.unionpay.sdk.request.*;
import com.github.shawven.calf.payment.support.*;
import com.github.shawven.calf.payment.provider.unionpay.sdk.request.*;
import com.github.shawven.calf.payment.support.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Shoven
 * @date 2019-09-17
 */
public class UnionpayClient {

    private String mchId;

    private String gatewayUrl;

    private String encryptKey;

    private String version;

    private String signMethod;

    private Charset charset;

    private int connectTimeout;

    private int readTimeout;

    private boolean useCert;

    private UnionpayCertification certification;

    private HttpUtil httpUtil;

    public UnionpayClient(String mchId, String gatewayUrl, String encryptKey, String version, Charset charset,
                          int connectTimeout, int readTimeout) {
        this.mchId = mchId;
        this.gatewayUrl = gatewayUrl;
        this.encryptKey = encryptKey;
        this.version = version;
        this.charset = charset;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.useCert = false;
        this.signMethod = UnionpayConstants.SIGNMETHOD_SHA256;
        this.httpUtil = PaymentContextHolder.getHttp();
    }

    public UnionpayClient(String mchId, String gatewayUrl, String signCertPath, String encryptCertPath,
                          String rootCertPath, String middleCertPath, String version, String signCertPassword,
                          boolean validateCNName, Charset charset, int connectTimeout, int readTimeout) {
        this.mchId = mchId;
        this.gatewayUrl = gatewayUrl;
        this.version = version;
        this.charset = charset;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.useCert = true;
        this.signMethod = UnionpayConstants.SIGNMETHOD_RSA;
        this.httpUtil = PaymentContextHolder.getHttp();
        this.certification = new UnionpayCertification(signCertPath, encryptCertPath, rootCertPath, middleCertPath,
                signCertPassword, validateCNName);
    }

    /**
     * 网页支付下单
     *
     * @param request
     * @return
     * @throws UnionpayException
     */
    public Map<String, String> pageExecute(UnionpayTradePayRequest request) throws UnionpayException {
        Map<String, String> params = getTradePayParams(request);
        // 前台跳转地址
        params.put(UnionpayConstants.param_frontUrl, request.getReturnUrl());
        // 签名
        sign(params);
        // 生成表单
        String form = PaymentUtils.buildForm(gatewayUrl + UnionpayConstants.FRONT_TRANS_URL, params);

        Map<String, String> response = new HashMap<>();
        response.put("respCode", "00");
        response.put("form", form);
        return response;
    }

    /**
     * APP支付下单
     *
     * @param request
     * @return
     * @throws UnionpayException
     */
    public Map<String, String> appExecute(UnionpayTradePayRequest request) throws UnionpayException {
        Map<String, String> params = getTradePayParams(request);
        return execute(UnionpayConstants.APP_TRANS_URL, params);
    }

    /**
     * @param request
     * @return
     * @throws UnionpayException
     */
    public Map<String, String> execute(UnionpayTradePayRequest request) throws UnionpayException {
        Map<String, String> params = getTradePayParams(request);
        if (PaymentUtils.isBlankString(request.getAuthCode())) {
            params.put(UnionpayConstants.param_qrNo, request.getAuthCode());
        }
        if (PaymentUtils.isBlankString(request.getTermId())) {
            params.put(UnionpayConstants.param_termId, request.getTermId());
        }
        return execute(UnionpayConstants.BACK_TRANS_URL, params);
    }

    /**
     * 支付查询
     *
     * @param request
     * @return
     * @throws UnionpayException
     */
    public Map<String, String> query(UnionpayTradeQueryRequest request) throws UnionpayException {
        Map<String, String> params = getTradeBaseParams(request);
        params.put(UnionpayConstants.param_orderId, request.getOutTradeNo());
        return execute(UnionpayConstants.SINGLE_QUERY_URL, params);
    }

    /**
     * 冲正订单
     *
     * @param request
     * @return
     * @throws UnionpayException
     */
    public Map<String, String> reversal(UnionpayTradeReversalRequest request) throws UnionpayException {
        Map<String, String> params = getTradeBaseParams(request);
        params.put(UnionpayConstants.param_channelType, request.getChannelType());
        params.put(UnionpayConstants.param_orderId, request.getOutTradeNo());
        return execute(UnionpayConstants.BACK_TRANS_URL, params);
    }

    /**
     * 退款
     *
     * @param request
     * @return
     * @throws UnionpayException
     */
    public Map<String, String> refund(UnionpayTradeRefundRequest request) throws UnionpayException {
        Map<String, String> params = getTradeBaseParams(request);
        params.put(UnionpayConstants.param_channelType, request.getChannelType());
        // 这里的订单号是退款订单号，不是原订单号
        params.put(UnionpayConstants.param_orderId, request.getOutRefundNo());
        // 交易流水号
        params.put(UnionpayConstants.param_origQryId, request.getTradeNo());
        // 退款金额
        params.put(UnionpayConstants.param_txnAmt, request.getRefundAmount());
        params.put(UnionpayConstants.param_currencyCode, request.getCurrencyCode());
        // 不需要退款通知固定值
        params.put(UnionpayConstants.param_backUrl, "http://www.specialUrl.com");
        return execute(UnionpayConstants.SINGLE_QUERY_URL, params);
    }

    /**
     * 退款查询
     *
     * @param request
     * @return
     * @throws UnionpayException
     */
    public Map<String, String> refundQuery(UnionpayTradeRefundQueryRequest request) throws UnionpayException {
        Map<String, String> params = getTradeBaseParams(request);
        params.put(UnionpayConstants.param_orderId, request.getOutRefundNo());
        return execute(UnionpayConstants.SINGLE_QUERY_URL, params);
    }

    /**
     * 支付交易参数
     *
     * @param request
     * @return
     */
    private Map<String, String> getTradePayParams(UnionpayTradePayRequest request) {
        Map<String, String> params = getTradeBaseParams(request);
        // 渠道类型，这个字段区分B2C网关支付和手机wap支付
        params.put(UnionpayConstants.param_channelType, request.getChannelType());
        // 商户订单号
        params.put(UnionpayConstants.param_orderId, request.getOutTradeNo());
        // 交易币种（境内商户一般是156 人民币）
        params.put(UnionpayConstants.param_currencyCode, request.getCurrencyCode());
        // 交易金额，单位分，不要带小数点
        params.put(UnionpayConstants.param_txnAmt, request.getAmount());
        // 风控信息域 商品名称之类的
        params.put(UnionpayConstants.param_riskRateInfo, "{commodityName=" + request.getSubject() + "}");
        // 后台通知地址
        params.put(UnionpayConstants.param_backUrl, request.getNotifyUrl());
        return params;
    }

    /**
     * 交易基础参数
     *
     * @param request
     * @return
     */
    private Map<String, String> getTradeBaseParams(UnionpayTradeRequest request) {
        Map<String, String> params = new HashMap<>();
        //商户号
        params.put(UnionpayConstants.param_merId, mchId);
        //版本号
        params.put(UnionpayConstants.param_version, version);
        //字符集编码 可以使用UTF-8,GBK两种方式
        params.put(UnionpayConstants.param_encoding, charset.name());
        //签名方法
        params.put(UnionpayConstants.param_signMethod, signMethod);

        // 业务类型，B2C、B2B、付款码等
        params.put(UnionpayConstants.param_bizType, request.getBizType());
        // 交易类型
        params.put(UnionpayConstants.param_txnType, request.getTradeType());
        // 交易子类型  01：消费
        params.put(UnionpayConstants.param_txnSubType, request.getTradeSubType());
        // 请求时间
        params.put(UnionpayConstants.param_txnTime, request.getRequestTime());
        //接入类型，商户接入固定填0，不需修改
        params.put(UnionpayConstants.param_accessType, request.getAccessType());
        return params;
    }

    private Map<String, String> execute(String relativeUrl, Map<String, String> params) throws UnionpayException {
        // 签名
        sign(params);

        // 请求
        String rsp;
        try {
            rsp = httpUtil.post(gatewayUrl + relativeUrl, params, connectTimeout, readTimeout);
        } catch (IOException e) {
            throw new UnionpayException(e);
        }

        // 解析响应
        Map<String, String> body = parseResponseBody(rsp);
        if (body.isEmpty()) {
            if (PaymentUtils.isBlankString(rsp)) {
                throw new UnionpayException("响应内容为空");
            }
            throw new UnionpayException("解析响应内容失败：" + rsp);
        }

        // 验签
        if (!verify(body)) {
            throw new UnionpayException("验签失败");
        }
        return body;
    }

    private void sign(Map<String, String> params) throws UnionpayException {
        params.put(UnionpayConstants.param_certId, certification.getSignCertId());
        String sign;
        String source = PaymentUtils.toPairString(params);

        if (useCert) {
            PrivateKey privateKey = certification.getSignCertPrivateKey();
            String content = SecurityUtils.sha2HexString(source, charset);
            sign = RsaUtils.rsa2Sign(content, privateKey, charset);
        } else {
            source = source + UnionpayConstants.AMPERSAND + SecurityUtils.sha2HexString(encryptKey, charset);
            sign = SecurityUtils.sha2HexString(source, charset);
        }
        params.put(UnionpayConstants.param_signature, sign);
    }

    public boolean verify(Map<String, String> response) throws UnionpayException {
        String sign = response.remove(UnionpayConstants.param_signature);

        // 解决同步回调证书内容有+号的问题
        String cert = response.get(UnionpayConstants.param_signPubKeyCert);
        response.put(UnionpayConstants.param_signPubKeyCert,
                cert.replace("BEGIN+CERTIFICATE", "BEGIN CERTIFICATE").replace("END+CERTIFICATE", "END CERTIFICATE"));

        String source = PaymentUtils.toPairString(response);

        if (useCert) {
            String certString = response.get(UnionpayConstants.param_signPubKeyCert);
            X509Certificate certificate;
            try {
                certificate = CertUtils.getCertificate(certString.getBytes());
            } catch (Exception e) {
                throw new UnionpayException("验签失败：获取银联证书公钥错误", e);
            }
            if (!certification.verifyCertificate(certificate)) {
                return false;
            }
            PublicKey publicKey = certificate.getPublicKey();
            String content = SecurityUtils.sha2HexString(source, charset);
            return RsaUtils.rsa2Verify(content, sign, publicKey, charset);
        } else {
            source = source + UnionpayConstants.AMPERSAND + SecurityUtils.sha2HexString(encryptKey, charset);
            return Objects.equals(sign, SecurityUtils.sha2HexString(source, charset));
        }
    }

    private Map<String, String> parseResponseBody(String rsp) {
        return PaymentUtils.splitPairString(rsp);
    }
}
