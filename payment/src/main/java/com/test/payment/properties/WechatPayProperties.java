package com.test.payment.properties;

import com.test.payment.properties.PaymentProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Shoven
 * @date 2019-09-02
 */
@Configuration
@PropertySource("classpath:payment.properties")
@ConfigurationProperties(prefix = "wechat")
public class WechatPayProperties implements PaymentProperties {

    private String appId;

    private String mchId;

    private String appKey;

    private String certUrl;

    private String notifyUrl;

    private Boolean useSandbox;

    private Boolean autoReport;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getCertUrl() {
        return certUrl;
    }

    public void setCertUrl(String certUrl) {
        this.certUrl = certUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public Boolean getUseSandbox() {
        return useSandbox;
    }

    public void setUseSandbox(Boolean useSandbox) {
        this.useSandbox = useSandbox;
    }

    public Boolean getAutoReport() {
        return autoReport;
    }

    public void setAutoReport(Boolean autoReport) {
        this.autoReport = autoReport;
    }
}
