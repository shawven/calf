package com.test.security.app.config;

import com.test.security.core.authentication.configurer.AuthorizationConfigurerProvider;
import com.test.security.core.properties.OAuth2Constants;
import com.test.security.social.property.SocialConstants;
import com.test.security.social.property.SocialProperties;
import com.test.security.validate.property.ValidationConstants;
import com.test.security.validate.property.ValidationProperties;
import org.apache.commons.lang3.StringUtils;
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
                OAuth2Constants.DEFAULT_SIGN_IN_PROCESSING_URL_FORM,
                OAuth2Constants.DEFAULT_TOKEN_PROCESSING_URL_MOBILE,
                SocialConstants.DEFAULT_TOKEN_PROCESSING_URL_OPENID,
                SocialConstants.DEFAULT_CURRENT_SOCIAL_USER_INFO_URL,
                ValidationConstants.DEFAULT_VALIDATE_CODE_URL_PREFIX + "/*",
                socialProperties.getFilterProcessesUrl() + "/*");

        return false;
    }
}
