package com.starter.security.social.properties;

/**
 * @author Shoven
 * @date 2019-08-19
 */
public interface SocialConstants {
    /**
     * 默认的OPENID登录请求处理url
     */
    String DEFAULT_TOKEN_PROCESSING_URL_OPENID = "/login/connect";

    /**
     * 社交登陆需要注册时获取用户信息的处理url
     */
    String DEFAULT_CURRENT_SOCIAL_USER_INFO_URL = "/social/user";

    /**
     * openid参数名
     */
    String DEFAULT_PARAMETER_NAME_OPENID = "openId";

    /**
     * providerId参数名
     */
    String DEFAULT_PARAMETER_NAME_PROVIDERID = "providerId";
}
