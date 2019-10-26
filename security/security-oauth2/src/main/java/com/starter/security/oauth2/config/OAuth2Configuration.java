
package com.starter.security.oauth2.config;

import com.starter.security.oauth2.DefaultUserDetailsService;
import com.starter.security.oauth2.properties.OAuth2Properties;
import com.starter.security.verification.annotation.EnableVerificationSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * 认证相关的扩展点配置。配置在这里的bean，业务系统都可以通过声明同类型或同名的bean来覆盖安全
 * 模块默认的配置。
 */
@Configuration
@EnableVerificationSupport
@EnableConfigurationProperties(OAuth2Properties.class)
public class OAuth2Configuration {

	/**
	 * 客户端密码处理器
	 * @return
	 */
	@Bean
    @ConditionalOnMissingBean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 默认认证器
	 *
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public UserDetailsService userDetailsService() {
		return new DefaultUserDetailsService();
	}
}
