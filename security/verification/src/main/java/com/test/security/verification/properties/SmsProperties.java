package com.test.security.verification.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-08-16
 */
public class SmsProperties {
    /**
     * 验证码长度
     */
    private int length = 6;

    /**
     * 过期时间
     */
    private int expireIn = 60;

    private SmsProvider lc;

    /**
     * 要拦截的url，多个url用逗号隔开，ant pattern
     */
    private String url;

    public int getLength() {
        return length;
    }

    public void setLength(int lenght) {
        this.length = lenght;
    }

    public int getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(int expireIn) {
        this.expireIn = expireIn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SmsProvider getLc() {
        return lc;
    }

    public void setLc(SmsProvider lc) {
        this.lc = lc;
    }

    @Configuration
    public static class SmsProvider {

        @Value("${sms.lc.url}")
        private String url;

        @Value("${sms.lc.appId}")
        private String appId;

        @Value("${sms.lc.secret}")
        private String secret;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

}

