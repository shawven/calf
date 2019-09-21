package com.test.payment.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author Shoven
 * @date 2019-09-03
 */
@Configuration
@PropertySource("classpath:payment.properties")
public class PaymentProperties {

    @Value("${serverUrl")
    private String serverUrl;

    @Value("${currencyCents}")
    private Long currencyCents;

    @Value("${useSandbox}")
    private Boolean useSandbox;

    @Value("${appName}")
    private String appName;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public Long getCurrencyCents() {
        return currencyCents;
    }

    public void setCurrencyCents(Long currencyCents) {
        this.currencyCents = currencyCents;
    }

    public Boolean getUseSandbox() {
        return useSandbox;
    }

    public void setUseSandbox(Boolean useSandbox) {
        this.useSandbox = useSandbox;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServerDomain() {
        // 去除端口
        return getServerUrl().replaceAll("(:\\d{2,})", "");
    }
}
