package com.test.payment.supplier.wechat;

import com.test.payment.properties.WechatPayProperties;
import com.test.payment.supplier.wechat.sdk.IWXPayDomain;
import com.test.payment.supplier.wechat.sdk.WXPay;
import com.test.payment.supplier.wechat.sdk.WXPayConfig;
import com.test.payment.supplier.wechat.sdk.WXPayConstants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shoven
 * @date 2019-09-02
 */
public class WechatPayClientFactory {

    private static WXPay client = null;

    public static WXPay getInstance(WechatPayProperties prop) {
        if (prop.getUseSandbox() || client == null) {
            client = new WXPay(new WxConfig(prop, true), prop.getNotifyUrl(),
                    prop.getAutoReport(), prop.getUseSandbox());
        }
        return new WXPay(new WxConfig(prop, true), prop.getNotifyUrl(),
                prop.getAutoReport(), prop.getUseSandbox());
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
                key = properties.getApiKey();
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
        public List<IWXPayDomain> getDomainList() {
            return DomainHolder.getDomainList();
        }

        @Override
        public boolean shouldAutoReport() {
            return properties.getAutoReport();
        }

    }

    static class DomainHolder {
        public static List<IWXPayDomain> getDomainList() {
            List<IWXPayDomain> domainList = new ArrayList<>();
            domainList.add(new DomainHolder.MasterDomain());
            domainList.add(new DomainHolder.SlaveDomain());
            return domainList;
        }


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
