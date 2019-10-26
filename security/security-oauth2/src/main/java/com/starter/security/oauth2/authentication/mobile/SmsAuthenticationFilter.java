
package com.starter.security.oauth2.authentication.mobile;

import com.starter.security.base.InvalidArgumentException;
import com.starter.security.oauth2.properties.OAuth2Constants;
import com.starter.security.verification.properties.VerificationConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 短信登录过滤器
 */
public class SmsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	// ~ Static fields/initializers
	// =====================================================================================

	private String mobileParameter = VerificationConstants.DEFAULT_PARAMETER_NAME_MOBILE;

	// ~ Constructors
	// ===================================================================================================

	public SmsAuthenticationFilter() {
		super(new AntPathRequestMatcher(OAuth2Constants.DEFAULT_TOKEN_PROCESSING_URL_MOBILE, "POST"));
	}

	// ~ Methods
	// ========================================================================================================

	@Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		if (!"POST".equals(request.getMethod())) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		String mobile = request.getParameter(mobileParameter);
        if (StringUtils.isBlank(mobile)) {
            throw new InvalidArgumentException("参数错误，手机号码不能为空");
        }

		SmsAuthenticationToken smsAuthenticationToken = new SmsAuthenticationToken(mobile.trim());
        smsAuthenticationToken.setDetails(authenticationDetailsSource.buildDetails(request));

		return this.getAuthenticationManager().authenticate(smsAuthenticationToken);
	}

	/**
	 * Sets the parameter name which will be used to obtain the username from
	 * the login request.
	 *
	 * @param usernameParameter
	 *            the parameter name. Defaults to "username".
	 */
	public void setMobileParameter(String usernameParameter) {
		Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
		this.mobileParameter = usernameParameter;
	}

	public final String getMobileParameter() {
		return mobileParameter;
	}

}
