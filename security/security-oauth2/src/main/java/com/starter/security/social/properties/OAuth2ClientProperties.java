
package com.starter.security.social.properties;

/**
 * 认证服务器注册的第三方应用配置项
 */

public class OAuth2ClientProperties {

	/**
	 * 第三方应用appId
	 */
	private String clientId;
	/**
	 * 第三方应用appSecret
	 */
	private String clientSecret;

	/**
	 *  accessToken的有效时间
	 */
	private int accessTokenValidateSeconds = 7200;

    /**
     * refreshToken的有效时间
     */
    private int refreshTokenValidateSeconds = 2592000;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public int getAccessTokenValidateSeconds() {
		return accessTokenValidateSeconds;
	}

	public void setAccessTokenValidateSeconds(int accessTokenValidateSeconds) {
		this.accessTokenValidateSeconds = accessTokenValidateSeconds;
	}

    public int getRefreshTokenValidateSeconds() {
        return refreshTokenValidateSeconds;
    }

    public void setRefreshTokenValidateSeconds(int refreshTokenValidateSeconds) {
        this.refreshTokenValidateSeconds = refreshTokenValidateSeconds;
    }
}
