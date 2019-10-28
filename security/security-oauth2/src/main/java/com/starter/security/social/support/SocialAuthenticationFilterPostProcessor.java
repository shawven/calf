
package com.starter.security.social.support;

import org.springframework.social.security.SocialAuthenticationFilter;

/**
 * 社交过滤器的后处理器，用于在不同环境下个性化社交登录的配置
 */
public interface SocialAuthenticationFilterPostProcessor {

	/**
	 * @param socialAuthenticationFilter
	 */
	void proceed(SocialAuthenticationFilter socialAuthenticationFilter);

}
