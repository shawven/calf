package com.test.security.social.property;

/**
 * @author Shoven
 * @since 2019-04-18 10:45
 */
public class ConnectProperties {

    protected String appId;

    protected String appSecret;

    protected String providerId;

    public ConnectProperties() {
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return this.appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
