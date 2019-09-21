package com.test.payment.supplier.unionpay.sdk;

import com.test.payment.supplier.unionpay.sdk.domain.UnionpayTradePagePayRequest;
import com.test.payment.supplier.unionpay.sdk.domain.UnionpayTradeQueryRequest;
import com.test.payment.supplier.unionpay.sdk.domain.UnionpayTradeRefundRequest;
import com.test.payment.support.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Logger logger = LoggerFactory.getLogger(UnionpayClient.class);

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
        this.certification = new UnionpayCertification(signCertPath, encryptCertPath, rootCertPath, middleCertPath,
               signCertPassword, validateCNName);
    }

    public String pagePay(UnionpayTradePagePayRequest request) throws UnionpayException {
        Map<String, String> params = new HashMap<>();
        //交易类型
        params.put(UnionpayConstants.param_txnType, request.getTradeType());
        //交易子类型
        params.put(UnionpayConstants.param_txnSubType, request.getTradeSubType());
        //业务类型，B2C网关支付，手机wap支付
        params.put(UnionpayConstants.param_bizType, request.getBizType());
        //渠道类型，这个字段区分B2C网关支付和手机wap支付
        params.put("channelType", request.getChannelType());
        // 商户订单号
        params.put(UnionpayConstants.param_orderId, request.getOutTradeNo());
        // 订单发送时间，每次发交易测试需修改为被查询的交易的订单发送时间
        params.put(UnionpayConstants.param_txnTime, request.getRequestTime());
        //交易币种（境内商户一般是156 人民币）
        params.put(UnionpayConstants.param_currencyCode, request.getCurrencyCode());
        //交易金额，单位分，不要带小数点
        params.put(UnionpayConstants.param_txnAmt, request.getAmount());
        // 风控信息域 商品名称之类的
        params.put("riskRateInfo", "{commodityName="+request.getSubject()+"}");
        // 前台跳转地址
        params.put(UnionpayConstants.param_frontUrl, request.getReturnUrl());
        // 后台通知地址
        params.put(UnionpayConstants.param_backUrl, request.getNotifyUrl());
        return pageExecute(UnionpayConstants.FRONT_TRANS_URL, params);
    }

    public Map<String, String> query(UnionpayTradeQueryRequest request) throws UnionpayException {
        Map<String, String> params = new HashMap<>();
        params.put(UnionpayConstants.param_txnType, request.getTradeType());
        params.put(UnionpayConstants.param_txnSubType, request.getTradeSubType());
        params.put(UnionpayConstants.param_bizType, request.getBizType());
        params.put(UnionpayConstants.param_orderId, request.getOutTradeNo());
        params.put(UnionpayConstants.param_txnTime, request.getRequestTime());

        return execute(UnionpayConstants.SINGLE_QUERY_URL, params);
    }

    public Map<String, String> refund(UnionpayTradeRefundRequest request) throws UnionpayException {
        Map<String, String> params = new HashMap<>();
        params.put(UnionpayConstants.param_txnType, request.getTradeType());
        params.put(UnionpayConstants.param_txnSubType, request.getTradeSubType());
        params.put(UnionpayConstants.param_bizType, request.getBizType());
        params.put(UnionpayConstants.param_orderId, request.getOutTradeNo());
        // 交易流水号
        params.put(UnionpayConstants.param_origQryId, request.getTradeNo());
        // 退款金额
        params.put(UnionpayConstants.param_txnAmt, request.getRefundAmount());
        params.put(UnionpayConstants.param_txnTime, request.getRequestTime());
        params.put(UnionpayConstants.param_currencyCode, request.getCurrencyCode());
        params.put(UnionpayConstants.param_backUrl, request.getNotifyUrl());
        return execute(UnionpayConstants.SINGLE_QUERY_URL, params);
    }

    private String pageExecute(String relativeUrl, Map<String, String> params) throws UnionpayException {
        putCommonParams(params);
        sign(params);
        return PaymentUtils.buildForm(gatewayUrl + relativeUrl, params);
    }

    private Map<String, String> execute(String relativeUrl, Map<String, String> params) throws UnionpayException {
        putCommonParams(params);
        sign(params);

        String rsp;
        try {
            rsp = PaymentHttpUtils.post(gatewayUrl + relativeUrl, params, connectTimeout, readTimeout);
        } catch (IOException e) {
            throw new UnionpayException(e);
        }
        Map<String, String> body = parseResponseBody(rsp);
        if (!verify(body)) {
            throw new UnionpayException("验签失败");
        }
        return body;
    }

    private void putCommonParams(Map<String, String> params) {
        //商户号
        params.put(UnionpayConstants.param_merId, mchId);
        //版本号
        params.put(UnionpayConstants.param_version, version);
        //字符集编码 可以使用UTF-8,GBK两种方式
        params.put(UnionpayConstants.param_encoding, charset.name());
        //接入类型，商户接入固定填0，不需修改
        params.put(UnionpayConstants.param_accessType, "0");
        //签名方法
        params.put(UnionpayConstants.param_signMethod, signMethod);
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
