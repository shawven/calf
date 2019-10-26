
package com.starter.security.app.config;

import com.starter.security.app.authentication.wxmini.WxMiniAuthenticationFilter;
import com.starter.security.app.authentication.wxmini.WxMiniAuthenticationProvider;
import com.starter.security.app.config.social.AppSingUpUtils;
import com.starter.security.social.properties.SocialProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class WxMiniAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private AuthenticationSuccessHandler appAuthenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler appAuthenticationFailureHandler;

	@Autowired
	private SocialUserDetailsService userDetailsService;

	@Autowired
    private AppSingUpUtils appSingUpUtils;

	@Autowired
    private SocialProperties socialProperties;

	@Override
	public void configure(HttpSecurity http) throws Exception {

        WxMiniAuthenticationFilter wxMiniAuthenticationFilter = new WxMiniAuthenticationFilter();
        wxMiniAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        wxMiniAuthenticationFilter.setAuthenticationSuccessHandler(appAuthenticationSuccessHandler);
        wxMiniAuthenticationFilter.setAuthenticationFailureHandler(appAuthenticationFailureHandler);
        wxMiniAuthenticationFilter.setAppSingUpUtils(appSingUpUtils);
        wxMiniAuthenticationFilter.setSecurityProperties(socialProperties);

        WxMiniAuthenticationProvider wxMiniAuthenticationProvider = new WxMiniAuthenticationProvider();
        wxMiniAuthenticationProvider.setUserDetailsService(userDetailsService);
        wxMiniAuthenticationProvider.setUsersConnectionRepository(appSingUpUtils.getUsersConnectionRepository());

		http.authenticationProvider(wxMiniAuthenticationProvider)
			.addFilterAfter(wxMiniAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

	}

}
