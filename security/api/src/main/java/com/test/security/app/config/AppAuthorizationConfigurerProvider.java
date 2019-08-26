package com.test.security.app.config;

import com.test.security.base.authentication.configurer.AuthorizationConfigurerProvider;
import com.test.security.oauth2.properties.OAuth2Constants;
import com.test.security.social.properties.SocialProperties;
import com.test.security.verification.properties.VerificationConstants;
import com.test.security.social.properties.SocialConstants;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

/**
 * @author Shoven
 * @date 2019-08-20
 */
public class AppAuthorizationConfigurerProvider implements AuthorizationConfigurerProvider {

    private SocialProperties socialProperties;

    public AppAuthorizationConfigurerProvider(SocialProperties socialProperties) {
        this.socialProperties = socialProperties;
    }

    @Override
    public boolean config(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config) {
        config.antMatchers(
                "/error",
                OAuth2Constants.DEFAULT_TOKEN_PROCESSING_URL_MOBILE,
                SocialConstants.DEFAULT_TOKEN_PROCESSING_URL_OPENID,
                SocialConstants.DEFAULT_CURRENT_SOCIAL_USER_INFO_URL,
                VerificationConstants.DEFAULT_VERIFICATION_URL_PREFIX + "/*",
                socialProperties.getFilterProcessesUrl() + "/*");

        return false;
    }
}
