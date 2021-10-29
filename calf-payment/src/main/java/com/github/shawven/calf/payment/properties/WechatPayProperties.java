package com.github.shawven.calf.payment.properties;

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
public class WechatPayProperties {

    private String appId;

    private String appSecret;

    private String mchId;

    private String apiKey;

    private String certUrl;

    private String notifyUrl;

    private Boolean useSandbox;

    private Boolean autoReport;

    private Integer reportWorkNum;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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

    public Integer getReportWorkNum() {
        return reportWorkNum;
    }

    public void setReportWorkNum(Integer reportWorkNum) {
        this.reportWorkNum = reportWorkNum;
    }
}
