
package com.starter.security.oauth2.config;

import com.starter.security.oauth2.authentication.mobile.SmsAuthenticationFilter;
import com.starter.security.oauth2.authentication.mobile.SmsAuthenticationProvider;
import com.starter.security.oauth2.MobileUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 短信登录配置
 */
@Component
public class SmsAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;

	@Autowired
	private AuthenticationFailureHandler authenticationFailureHandler;

	@Autowired
	private MobileUserDetailsService userDetailsService;

	@Autowired(required = false)
	private PersistentTokenRepository persistentTokenRepository;

	/**
     *  (non-Javadoc)
	 * @see org.springframework.security.config.annotation.SecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.SecurityBuilder)
	 */
	@Override
	public void configure(HttpSecurity http) throws Exception {

		SmsAuthenticationFilter smsAuthenticationFilter = new SmsAuthenticationFilter();
		smsAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		smsAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
		smsAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);

		if (persistentTokenRepository != null) {
            String key = UUID.randomUUID().toString();
            smsAuthenticationFilter.setRememberMeServices(
                    new PersistentTokenBasedRememberMeServices(key, userDetailsService, persistentTokenRepository));
        }

		SmsAuthenticationProvider smsAuthenticationProvider = new SmsAuthenticationProvider();
		smsAuthenticationProvider.setUserDetailsService(userDetailsService);

		http.authenticationProvider(smsAuthenticationProvider)
			.addFilterAfter(smsAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

	}

}
