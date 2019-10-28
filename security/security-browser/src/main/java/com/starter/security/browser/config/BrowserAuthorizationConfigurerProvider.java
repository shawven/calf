package com.starter.security.browser.config;

import com.starter.security.base.authentication.configurer.AuthorizationConfigurerProvider;
import com.starter.security.browser.properties.BrowserProperties;
import com.starter.security.social.properties.OAuth2Constants;
import com.starter.security.social.properties.SocialProperties;
import com.starter.security.verification.properties.VerificationConstants;
import com.starter.security.social.properties.SocialConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Shoven
 * @date 2019-08-20
 */
public class BrowserAuthorizationConfigurerProvider implements AuthorizationConfigurerProvider {

    private BrowserProperties browserProperties;

    private SocialProperties socialProperties;

    public BrowserAuthorizationConfigurerProvider(BrowserProperties browserProperties,
                                                  SocialProperties socialProperties) {
        this.browserProperties = browserProperties;
        this.socialProperties = socialProperties;
    }

    @Override
    public boolean config(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config) {
        String[] urls = {
                OAuth2Constants.DEFAULT_TOKEN_PROCESSING_URL_MOBILE,
                SocialConstants.DEFAULT_TOKEN_PROCESSING_URL_OPENID,
                SocialConstants.DEFAULT_CURRENT_SOCIAL_USER_INFO_URL,
                VerificationConstants.DEFAULT_VERIFICATION_URL_PREFIX + "/*",
                socialProperties.getFilterProcessesUrl() + "/*",
                browserProperties.getSignInUrl(),
                browserProperties.getSignUpUrl(),
                browserProperties.getSession().getSessionInvalidUrl()
        };
        config.antMatchers(Arrays.stream(urls).filter(s -> !s.isEmpty()).toArray(String[]::new)).permitAll();

        String signOutUrl = browserProperties.getSignOutSuccessUrl();
        if (StringUtils.isNotBlank(signOutUrl)) {
            config.antMatchers(signOutUrl).permitAll();
        }
        return false;
    }
}
