package com.github.shawven.calf.payment.provider.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.github.shawven.calf.payment.provider.alipay.sdk.AlipayConstants;
import com.github.shawven.calf.payment.properties.AlipayProperties;
import com.github.shawven.calf.payment.support.PaymentUtils;

public class AlipayClientFactory {

    private static AlipayClient client = null;

    /**
     * 封装公共请求参数
     *
     * @return AlipayClient
     */
    public static AlipayClient getInstance(AlipayProperties prop) {
        if (prop.getUseSandbox() || client == null) {
            // 网关
            String url = prop.getUseSandbox() ? AlipayConstants.SANDBOX_GATEWAY_URL : AlipayConstants.GATEWAY_URL;
            // 商户APP_ID
            String appId = prop.getAppId();
            // 商户RSA 私钥
            String privateKey = prop.getPrivateKey();
            // 请求方式 json
            String format = "json";
            // 编码格式，目前只支持UTF-8
            String charset = "UTF-8";
            // 支付宝公钥
            String publicKey = prop.getPublicKey();
            // 签名方式
            String signType = prop.getSignType();
            // 加密KEY
            String encryptKey = prop.getEncryptKey();
            if (PaymentUtils.isBlankString(encryptKey)) {
                client = new DefaultAlipayClient(url, appId, privateKey, format, charset, publicKey, signType);
            } else {
                client = new DefaultAlipayClient(url, appId, privateKey, format, charset, publicKey, signType, encryptKey, null);
            }
        }
        return client;
    }
}
