
package com.starter.security.social.config;

import com.starter.security.social.PhoneUserDetailsService;
import com.starter.security.social.mobile.SmsAuthenticationFilter;
import com.starter.security.social.mobile.SmsAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 短信登录配置
 */
public class SmsAuthenticationSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private PhoneUserDetailsService userDetailsService;

    private AuthenticationSuccessHandler authenticationSuccessHandler;

    private AuthenticationFailureHandler authenticationFailureHandler;

    public SmsAuthenticationSecurityConfigurer(PhoneUserDetailsService userDetailsService,
                                               AuthenticationSuccessHandler authenticationSuccessHandler,
                                               AuthenticationFailureHandler authenticationFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

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

		SmsAuthenticationProvider smsAuthenticationProvider = new SmsAuthenticationProvider();
		smsAuthenticationProvider.setUserDetailsService(userDetailsService);

		http.authenticationProvider(smsAuthenticationProvider)
			.addFilterAfter(smsAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

	}

}
