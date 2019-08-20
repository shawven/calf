
package com.test.security.social.property;

/**
 * 微信登录配置项
 */
public class WeixinProperties extends ConnectProperties {

	/**
	 * 第三方id，用来决定发起第三方登录的url，默认是 weixin。
	 */
	private String providerId = "weixin";

	private String filterProcessesUrl;

    /**
     * 微信小程序的流程和标准的oauth2不一样，单独配置
     */
    private String wxMiniProviderId = "wxmini";

    public void setFilterProcessesUrl(String filterProcessesUrl) {
        this.filterProcessesUrl = filterProcessesUrl;
    }

    public String getWxMiniProviderId() {
        return wxMiniProviderId;
    }

    public void setWxMiniProviderId(String wxMiniProviderId) {
        this.wxMiniProviderId = wxMiniProviderId;
    }

    public String getWxMiniProcessUrl() {
        return filterProcessesUrl + "/" + wxMiniProviderId;
    }

    public void setWxminiProcessUrl(String wxminiProcessUrl) {
        this.wxMiniProviderId = wxminiProcessUrl;
    }
}
