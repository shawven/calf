
package com.test.security.verification.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.verification")
public class VerificationProperties {

    /**
     * 图片验证码配置
     */
    private CaptchaProperties captcha;

    /**
     * 短信验证码配置
     */
    private SmsProperties sms;

    public CaptchaProperties getCaptcha() {
        return captcha;
    }

    public void setCaptcha(CaptchaProperties captcha) {
        this.captcha = captcha;
    }

    public SmsProperties getSms() {
        return sms;
    }

    public void setSms(SmsProperties sms) {
        this.sms = sms;
    }

}

