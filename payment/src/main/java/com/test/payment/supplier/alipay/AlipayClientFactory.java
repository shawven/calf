package com.test.payment.supplier.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.test.payment.properties.AlipayProperties;

public class AlipayClientFactory {

    private static AlipayClient client = null;

    /**
     * 封装公共请求参数
     *
     * @return AlipayClient
     */
    public static AlipayClient getInstance(AlipayProperties prop) {
        if (client == null) {
            // 网关
            String url = prop.getGatewayUrl();
            // 商户APP_ID
            String appId = prop.getAppId();
            // 商户RSA 私钥
            String privateKey = prop.getPrivateKey();
            // 请求方式 json
            String format = prop.getFormat();
            // 编码格式，目前只支持UTF-8
            String charset = prop.getCharset();
            // 支付宝公钥
            String publicKey = prop.getPublicKey();
            // 签名方式
            String signType = prop.getSignType();
            client = new DefaultAlipayClient(url, appId, privateKey, format, charset, publicKey, signType);
        }
        return client;
    }
}
