
package com.starter.security.social.mobile;

import com.starter.security.social.PhoneUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 短信登录验证逻辑
 *
 * 由于短信验证码的验证在过滤器里已完成，这里直接读取用户信息即可。
 */
public class SmsAuthenticationProvider implements AuthenticationProvider {

	private PhoneUserDetailsService userDetailsService;

	/**
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		SmsAuthenticationToken authenticationToken = (SmsAuthenticationToken) authentication;

        String mobile = (String) authenticationToken.getPrincipal();

        UserDetails user = userDetailsService.loadUserByPhone(mobile);

		SmsAuthenticationToken authenticationResult = new SmsAuthenticationToken(user, user.getAuthorities());
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
		return SmsAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public PhoneUserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(PhoneUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

}
