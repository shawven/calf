
package com.test.security.app.authentication.wxmini;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;

import java.util.HashSet;
import java.util.Set;


public class WxMiniAuthenticationProvider implements AuthenticationProvider {

	private SocialUserDetailsService userDetailsService;

    private UsersConnectionRepository usersConnectionRepository;

    /**
	 * (non-Javadoc)
	 *
	 * @see AuthenticationProvider#authenticate
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WxMiniAuthenticationToken token = (WxMiniAuthenticationToken) authentication;

        Set<String> providerUserIds = new HashSet<>();
        providerUserIds.add((String)token.getPrincipal());

        Set<String> userIds = usersConnectionRepository.findUserIdsConnectedTo(token.getProviderId(), providerUserIds);
        if(userIds == null || userIds.isEmpty()) {
            throw new BadCredentialsException("暂无关联信息");
        }

        SocialUserDetails user = userDetailsService.loadUserByUserId(userIds.iterator().next());
        if (user == null) {
            throw new BadCredentialsException("无法获取用户信息");
        }

        WxMiniAuthenticationToken wxMiniAuthenticationToken = new WxMiniAuthenticationToken(user, user.getAuthorities());
        wxMiniAuthenticationToken.setDetails(token.getDetails());

		return wxMiniAuthenticationToken;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return WxMiniAuthenticationToken.class.isAssignableFrom(authentication);
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
