package com.github.shawven.calf.payment.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Shoven
 * @date 2019-09-03
 */
@Configuration
@PropertySource("classpath:payment.properties")
public class AppProperties {

    /**
     * 服务器地址
     */
    @Value("${serverUrl}")
    private String serverUrl;

    /**
     * 货币基准（分） 订单金额以元为单位则100，以分为单位则是1
     */
    @Value("${currencyCents}")
    private Long currencyCents;

    /**
     * 是否使用沙箱环境（仅仅是起到placeholder的作用，实际具体要看配置文件每个提供商具体的值）
     */
    @Value("${useSandbox:true}")
    private Boolean useSandbox;

    /**
     * 应用名称（目前微信wap、公众号等需要使用）
     */
    @Value("${appName:}")
    private String appName;

    /**
     * 连接超时
     */
    @Value("${connectTimeout:0}")
    private int connectTimeout;

    /**
     * 读超时
     */
    @Value("${readTimeout:0}")
    private int readTimeout;

    /**
     * 连接池最大连接数
     */
    @Value("${maxTotal:0}")
    private int maxTotal;

    /**
     * 连接池每个路由最大连接数
     */
    @Value("${maxPerRoute:0}")
    private int maxPerRoute;

    /**
     * 连接池路由存活时间
     */
    @Value("${connectionTimeToLive:0}")
    private int connectionTimeToLive;

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

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
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

    public int getConnectionTimeToLive() {
        return connectionTimeToLive;
    }

    public void setConnectionTimeToLive(int connectionTimeToLive) {
        this.connectionTimeToLive = connectionTimeToLive;
    }

    public String getServerDomain() {
        // 去除端口
        return getServerUrl().replaceAll("(:\\d{2,})", "");
    }
}
