
package com.starter.security.app.authentication.openid;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.SocialUserDetailsService;

import java.util.HashSet;
import java.util.Set;


public class OpenIdAuthenticationProvider implements AuthenticationProvider {

	private SocialUserDetailsService userDetailsService;

	private UsersConnectionRepository usersConnectionRepository;

	/**
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#authenticate
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		OpenIdAuthenticationToken authenticationToken = (OpenIdAuthenticationToken) authentication;

        String providerId = authenticationToken.getProviderId();
        String openId = (String)authenticationToken.getPrincipal();

        Set<String> providerUserIds = new HashSet<>();
		providerUserIds.add(openId);
		Set<String> userIds = usersConnectionRepository.findUserIdsConnectedTo(providerId, providerUserIds);

		if(userIds == null || userIds.size() != 1) {
            throw new BadCredentialsException("无效的providerId或openId，无法获取用户信息");
        }

		String userId = userIds.iterator().next();

		UserDetails user = userDetailsService.loadUserByUserId(userId);
		if (user == null) {
			throw new BadCredentialsException("无法获取用户信息");
		}

		OpenIdAuthenticationToken authenticationResult = new OpenIdAuthenticationToken(user, user.getAuthorities());
		authenticationResult.setDetails(authenticationToken.getDetails());

		return authenticationResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return OpenIdAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public SocialUserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(SocialUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public UsersConnectionRepository getUsersConnectionRepository() {
		return usersConnectionRepository;
	}

	public void setUsersConnectionRepository(UsersConnectionRepository usersConnectionRepository) {
		this.usersConnectionRepository = usersConnectionRepository;
	}

}
