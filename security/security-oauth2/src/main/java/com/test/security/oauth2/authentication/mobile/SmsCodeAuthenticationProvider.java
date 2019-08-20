
package com.test.security.oauth2.authentication.mobile;

import com.test.security.oauth2.MobileUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 短信登录验证逻辑
 *
 * 由于短信验证码的验证在过滤器里已完成，这里直接读取用户信息即可。
 */
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

	private MobileUserDetailsService userDetailsService;

	/**
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;

        String mobile = (String) authenticationToken.getPrincipal();

        UserDetails user = userDetailsService.loadUserByMobileNumber(mobile);

		SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(user, user.getAuthorities());
		authenticationResult.setDetails(authenticationToken.getDetails());

		return authenticationResult;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public MobileUserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(MobileUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

}
