
package com.test.security.validate.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.code")
public class ValidationProperties {

    /**
     * 图片验证码配置
     */
    private ImageCodeProperties image;

    /**
     * 短信验证码配置
     */
    private SmsCodeProperties sms;

    public ImageCodeProperties getImage() {
        return image;
    }

    public void setImage(ImageCodeProperties image) {
        this.image = image;
    }

    public SmsCodeProperties getSms() {
        return sms;
    }

    public void setSms(SmsCodeProperties sms) {
        this.sms = sms;
    }

}

