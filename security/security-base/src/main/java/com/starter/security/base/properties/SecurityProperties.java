package com.starter.security.base.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-10-28
 */
@ConfigurationProperties("app.security")
public class SecurityProperties {

    private String whitelist;

    public String getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(String whitelist) {
        this.whitelist = whitelist;
    }
}
