
package com.test.security.social.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 社交登录配置项
 */
@ConfigurationProperties("app.security.social")
public class SocialProperties {

	/**
	 * 社交登录功能拦截的url
	 */
	private String filterProcessesUrl = "/login/connect";

    /**
     * 社交登录，如果需要用户注册，跳转的页面
     */
    private String signUpUrl = SocialConstants.DEFAULT_CURRENT_SOCIAL_USER_INFO_URL;

	private QQProperties qq = new QQProperties();

	private WeixinProperties weixin = new WeixinProperties();

    public String getFilterProcessesUrl() {
        return filterProcessesUrl;
    }

    public void setFilterProcessesUrl(String filterProcessesUrl) {
        this.filterProcessesUrl = filterProcessesUrl;
    }

    public String getSignUpUrl() {
        return signUpUrl;
    }

    public void setSignUpUrl(String signUpUrl) {
        this.signUpUrl = signUpUrl;
    }

    public QQProperties getQq() {
        return qq;
    }

    public void setQq(QQProperties qq) {
        this.qq = qq;
    }

    public WeixinProperties getWeixin() {
        return weixin;
    }

    public void setWeixin(WeixinProperties weixin) {
        this.weixin = weixin;
    }
}
