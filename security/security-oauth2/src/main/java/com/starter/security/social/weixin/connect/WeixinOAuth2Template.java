
package com.starter.security.social.weixin.connect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.security.base.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 完成微信的OAuth2认证流程的模板类。国内厂商实现的OAuth2每个都不同, spring默认提供的OAuth2Template适应不了，只能针对每个厂商自己微调。
 */
public class WeixinOAuth2Template extends OAuth2Template {

    private String clientId;

    private String clientSecret;

    private String accessTokenUrl;

    private static final String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ObjectMapper objectMapper = new ObjectMapper();

    public WeixinOAuth2Template(String clientId, String clientSecret, String authorizeUrl, String accessTokenUrl) {
        super(clientId, clientSecret, authorizeUrl, accessTokenUrl);
        setUseParametersForClientAuthentication(true);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessTokenUrl = accessTokenUrl;
    }

    /**
     * (non-Javadoc)
     *
     * @see org.springframework.social.oauth2.OAuth2Template#exchangeForAccess(java.lang.String, java.lang.String, org.springframework.util.MultiValueMap)
     */
    @Override
    public AccessGrant exchangeForAccess(String authorizationCode, String redirectUri,
                                         MultiValueMap<String, String> parameters) {

        StringBuilder accessTokenRequestUrl = new StringBuilder(accessTokenUrl);

        accessTokenRequestUrl.append("?appid=" + clientId);
        accessTokenRequestUrl.append("&secret=" + clientSecret);
        accessTokenRequestUrl.append("&code=" + authorizationCode);
        accessTokenRequestUrl.append("&grant_type=authorization_code");
        accessTokenRequestUrl.append("&redirect_uri=" + redirectUri);

        return getAccessToken(accessTokenRequestUrl);
    }

    @Override
    public AccessGrant refreshAccess(String refreshToken, MultiValueMap<String, String> additionalParameters) {
        StringBuilder refreshTokenUrl = new StringBuilder(REFRESH_TOKEN_URL);

        refreshTokenUrl.append("?appid=" + clientId);
        refreshTokenUrl.append("&grant_type=refresh_token");
        refreshTokenUrl.append("&refresh_token=" + refreshToken);

        return getAccessToken(refreshTokenUrl);
    }

    @SuppressWarnings("unchecked")
    private AccessGrant getAccessToken(StringBuilder accessTokenRequestUrl) {
        String response = getRestTemplate().getForObject(accessTokenRequestUrl.toString(), String.class);
//        String response = "{\"access_token\":\"20_OjoL-GvnjMYFn5YSluEp3y1MmthbcaNRuh3bLk6RHyjA2SpA2Y58w86pcQT6r_b9102AIFZ5OhIKeIKY_wV98g\",\"expires_in\":7200,\"refresh_token\":\"20_fRN5EVnN4c-LM5dyhJW88jZV17EgHzE_XvZacurLdoJyBjUaAr12h0iKwL32JkTxoQAV5gu2GzMjzEdgVfh8OQ\",\"openid\":\"od4PTw7Iijvj9qAw3RtLXSUZpMOU\",\"scope\":\"snsapi_login\",\"unionid\":\"oEg8VuH4KoidJSzmviOKsY9n7igU\"}";
        logger.info("获取weixin access_token响应: " + response);

        Map<String, Object> result;
        try {
            result = objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        //返回错误码时直接返回空
        if (result.get("errcode") != null) {
            String errCode = String.valueOf(result.get("errcode"));
            String errMsg = String.valueOf(result.get("errmsg"));
            throw new InvalidArgumentException("获取weixin access token失败, errcode:" + errCode + ", errmsg:" + errMsg);
        }

        WeixinAccessGrant accessToken = new WeixinAccessGrant(
                result.get("access_token").toString(),
                result.get("scope").toString(),
                result.get("refresh_token").toString(),
                Long.valueOf(result.get("expires_in").toString()));

        accessToken.setOpenId(result.get("openid").toString());

        return accessToken;
    }

    /**
     * 构建获取授权码的请求。也就是引导用户跳转到微信的地址。
     *
     * @param parameters
     * @return
     */
    @Override
    public String buildAuthenticateUrl(OAuth2Parameters parameters) {
        String url = super.buildAuthenticateUrl(parameters);
        url = url + "&appid=" + clientId + "&scope=snsapi_login";
        return url;
    }

    @Override
    public String buildAuthorizeUrl(OAuth2Parameters parameters) {
        return buildAuthenticateUrl(parameters);
    }

    /**
     * 微信返回的contentType是html/text，添加相应的HttpMessageConverter来处理。
     */
    @Override
    protected RestTemplate createRestTemplate() {
        RestTemplate restTemplate = super.createRestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

}
