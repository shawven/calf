package com.github.shawven.calf.payment.provider.wechat;

import com.github.shawven.calf.payment.provider.wechat.sdk.IWXPayDomain;
import com.github.shawven.calf.payment.provider.wechat.sdk.WXPay;
import com.github.shawven.calf.payment.provider.wechat.sdk.WXPayConfig;
import com.github.shawven.calf.payment.provider.wechat.sdk.WXPayConstants;
import com.github.shawven.calf.payment.properties.WechatPayProperties;

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
            WxConfig wxConfig = new WxConfig(prop);
            if (client != null) {
                WXPayConfig oldConfig = client.getConfig();
                if (oldConfig.isExistSandboxKey()) {
                    wxConfig.setSandBoxKey(oldConfig.getKey());
                }
            }
            client = new WXPay(wxConfig, prop.getNotifyUrl(), prop.getAutoReport(), prop.getUseSandbox());
        }
        return client;
    }

    private static class WxConfig extends WXPayConfig {

        private WechatPayProperties properties;

        private boolean existSandboxKey;

        private String sandboxKey;

        public WxConfig(WechatPayProperties properties) {
            this.properties = properties;
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
            if (properties.getUseSandbox() && existSandboxKey) {
                return sandboxKey;
            }
            return properties.getApiKey();
        }

        @Override
        public void setSandBoxKey(String sandBoxKey) {
            this.sandboxKey = sandBoxKey;
            this.existSandboxKey = true;
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

        @Override
        public int getReportWorkerNum() {
            return properties.getReportWorkNum();
        }

        @Override
        public boolean isExistSandboxKey() {
            return existSandboxKey;
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
        }

        ;

        static class SlaveDomain implements IWXPayDomain {
            @Override
            public void report(long elapsedTimeMillis, Exception ex) {
            }

            @Override
            public IWXPayDomain.DomainInfo getDomain() {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API2, false);
            }
        }

        ;
    }
}
