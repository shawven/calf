package com.test.payment.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    private int connectTimeout;

    private int socketTimeout;

    private int connectionTimeToLive;

    private int maxTotal;

    private int maxPerRoute;

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
        this.appName = appName != null
            ? new String(appName.getBytes(ISO_8859_1), UTF_8)
            : "";
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionTimeToLive() {
        return connectionTimeToLive;
    }

    public void setConnectionTimeToLive(int connectionTimeToLive) {
        this.connectionTimeToLive = connectionTimeToLive;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public String getServerDomain() {
        // 去除端口
        return getServerUrl().replaceAll("(:\\d{2,})", "");
    }
}
