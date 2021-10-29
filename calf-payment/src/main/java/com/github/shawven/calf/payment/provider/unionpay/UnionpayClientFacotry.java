package com.github.shawven.calf.payment.provider.unionpay;

import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayClient;
import com.github.shawven.calf.payment.provider.unionpay.sdk.UnionpayConstants;
import com.github.shawven.calf.payment.properties.UnionpayProperties;
import com.github.shawven.calf.payment.support.PaymentUtils;

import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author Shoven
 * @date 2019-09-17
 */
public class UnionpayClientFacotry {

    private static UnionpayClient b2cClient;
    private static UnionpayClient b2bClient;

    public static UnionpayClient getB2CInstance(UnionpayProperties prop) {
        b2cClient = loadInstance(b2cClient, prop);
        return b2cClient;
    }

    public static UnionpayClient getB2BInstance(UnionpayProperties prop) {
        b2bClient = loadInstance(b2bClient, prop);
        return b2bClient;
    }

    public static UnionpayClient loadInstance(UnionpayClient client, UnionpayProperties prop) {
        if (prop.getUseSandbox() || client == null) {
            String gatewayUrl = prop.getUseSandbox()
                    ? UnionpayConstants.SANDBOX_GATEWAY_URL
                    : UnionpayConstants.GATEWAY_URL;
            boolean useCert = PaymentUtils.isBlankString(prop.getEncryptKey());
            if (useCert) {
                ClassLoader classLoader = UnionpayClientFacotry.class.getClassLoader();
                URL signCertUrl = classLoader.getResource(prop.getSignCertPath());
                if (signCertUrl == null) {
                    throw new RuntimeException(String.format("签名证书[%s]不存在", prop.getSignCertPath()));
                }
                URL encryptCertUrl = classLoader.getResource(prop.getEncryptCertPath());
                if (encryptCertUrl == null) {
                    throw new RuntimeException(String.format("加密证书[%s]不存在", prop.getSignCertPath()));
                }
                URL rootCertUrl = classLoader.getResource(prop.getRootCertPath());
                if (rootCertUrl == null) {
                    throw new RuntimeException(String.format("银联根证书[%s]不存在", prop.getSignCertPath()));
                }
                URL middleCertUrl = classLoader.getResource(prop.getMiddleCertPath());
                if (middleCertUrl == null) {
                    throw new RuntimeException(String.format("银联中间证书[%s]不存在", prop.getSignCertPath()));
                }

                client = new UnionpayClient(prop.getMchId(), gatewayUrl, signCertUrl.getPath(), encryptCertUrl.getPath(),
                        rootCertUrl.getPath(), middleCertUrl.getPath(), UnionpayConstants.VERSION_5_1_0,
                        prop.getSignCertPassword(), prop.getValidateCnName(), Charset.forName(prop.getCharset()),
                        3000, 15000);
            } else {
                client = new UnionpayClient(prop.getMchId(), gatewayUrl, prop.getEncryptKey(),
                        UnionpayConstants.VERSION_5_1_0, Charset.forName(prop.getCharset()),
                        3000, 15000);
            }
        }
        return client;
    }
}
