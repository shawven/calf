
package com.test.security.base.authentication.configurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 授权信息管理器
 *
 * 用于收集系统中所有 ConfigProvider 并加载其配置
 */
@Component
public class AuthorizationConfigurerManager {

	@Autowired
	private List<AuthorizationConfigurerProvider> authorizationConfigurerProviders;


	public void config(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config) {
		boolean existAnyRequestConfig = false;
		String existAnyRequestConfigName = null;

		for (AuthorizationConfigurerProvider authorizeConfigProvider : authorizationConfigurerProviders) {
			boolean currentIsAnyRequestConfig = authorizeConfigProvider.config(config);

			if (existAnyRequestConfig && currentIsAnyRequestConfig) {
				throw new RuntimeException("重复的anyRequest配置:" + existAnyRequestConfigName + ","
						+ authorizeConfigProvider.getClass().getSimpleName());
			} else if (currentIsAnyRequestConfig) {
				existAnyRequestConfig = true;
				existAnyRequestConfigName = authorizeConfigProvider.getClass().getSimpleName();
			}
		}

		if(existAnyRequestConfig){
			config.anyRequest().authenticated();
		}
	}

}
