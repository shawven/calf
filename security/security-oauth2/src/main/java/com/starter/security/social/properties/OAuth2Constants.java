
package com.starter.security.social.properties;

/**
 * security常量
 *
 * @author Shoven
 * @since 2019-05-08 21:52
 */
public interface OAuth2Constants {

    /**
     * 默认的授权端点（获取和刷新令牌端点）
     * 原材上不应该修改，为了统一登陆前缀
     * 密码登陆（获取令牌、刷新令牌）：/login/token
     * 手机登陆（获取令牌）：/login/mobile
     * 社交登陆（获取令牌）：/login/connect/qq、/login/connect/weixin 等
     */
    String DEFAULT_OAUTH_TOKEN_ENDPOINTS = "/login/token";

	/**
	 * 默认的手机验证码登录请求处理url
	 */
	String DEFAULT_TOKEN_PROCESSING_URL_MOBILE = "/login/mobile";
}
