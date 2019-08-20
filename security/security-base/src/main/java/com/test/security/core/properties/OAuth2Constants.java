
package com.test.security.core.properties;

/**
 * security常量
 *
 * @author Shoven
 * @since 2019-05-08 21:52
 */
public interface OAuth2Constants {

	/**
	 * 默认的用户名密码登录请求处理url
	 */
	String DEFAULT_SIGN_IN_PROCESSING_URL_FORM = "/login/form";

	/**
	 * 默认的手机验证码登录请求处理url
	 */
	String DEFAULT_TOKEN_PROCESSING_URL_MOBILE = "/oauth/mobile/token";
}
