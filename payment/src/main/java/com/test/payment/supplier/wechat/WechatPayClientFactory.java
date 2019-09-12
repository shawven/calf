package com.test.payment.supplier.wechat;

import com.test.payment.properties.WechatPayProperties;
import com.test.payment.supplier.wechat.sdk.IWXPayDomain;
import com.test.payment.supplier.wechat.sdk.WXPay;
import com.test.payment.supplier.wechat.sdk.WXPayConfig;
import com.test.payment.supplier.wechat.sdk.WXPayConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Shoven
 * @date 2019-09-02
 */
public class WechatPayClientFactory {

    private static WXPay client = null;
    private static WXPay client2 = null;

    public static WXPay getInstance(WechatPayProperties properties) {
        if (properties.getUseSandbox()) {
            // 沙箱环境实例化时会自动获取沙箱密钥
            try {
                return getInstance1(properties);
            } catch (Exception e) {
                return getInstance2(properties);
            }
        }
        return getInstance1(properties);
    }

    /**
     * 主域名
     *
     * @param properties
     * @return
     */
    private static WXPay getInstance1(WechatPayProperties properties) {
        if (client == null) {
            client = new WXPay(new WxConfig(properties, true), properties.getNotifyUrl(),
                    properties.getAutoReport(), properties.getUseSandbox());
        }
        return client;
    }

    /**
     * 备用域名
     *
     * @param properties
     * @return
     */
    public static WXPay getInstance2(WechatPayProperties properties) {
        if (client2 == null) {
            client2 = new WXPay(new WxConfig(properties, false), properties.getNotifyUrl(),
                    properties.getAutoReport(), properties.getUseSandbox());
        }
        return client2;
    }

    private static class WxConfig extends WXPayConfig {

        private WechatPayProperties properties;

        private String key;

        private boolean slaveModel;

        public WxConfig(WechatPayProperties properties, boolean slaveModel) {
            this.properties = properties;
            this.slaveModel = slaveModel;
        }

        @Override
        public String getAppID() {
            return properties.getAppId();
        }

        @Override
        public String getMchID() {
            return properties.getMchId();
        }

        @Override
        public String getKey() {
            if (key == null) {
                key = properties.getAppKey();
            }
            return key;
        }

        @Override
        public void setKey(String key) {
            if (properties.getUseSandbox()) {
                this.key = key;
            }
        }

        @Override
        public InputStream getCertStream() {
            return this.getClass().getClassLoader().getResourceAsStream(properties.getCertUrl());
        }

        @Override
        public IWXPayDomain getWXPayDomain() {
            return slaveModel ? new DomainHolder.SlaveDomain() : new DomainHolder.MasterDomain();
        }
        @Override
        public boolean shouldAutoReport() {
            return properties.getAutoReport();
        }

    }

    static class DomainHolder {
        static class MasterDomain implements IWXPayDomain {
            @Override
            public void report(long elapsedTimeMillis, Exception ex) {
            }

            @Override
            public IWXPayDomain.DomainInfo getDomain() {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API, true);
            }
        };

        static class SlaveDomain implements IWXPayDomain {
            @Override
            public void report(long elapsedTimeMillis, Exception ex) {
            }

            @Override
            public IWXPayDomain.DomainInfo getDomain() {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API2, false);
            }
        };
    }
}
