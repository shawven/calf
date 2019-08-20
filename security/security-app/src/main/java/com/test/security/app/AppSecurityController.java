package com.test.security.app;

import com.test.security.app.config.social.AppSingUpUtils;
import com.test.security.core.ResponseData;
import com.test.security.social.SocialController;
import com.test.security.social.property.SocialConstants;
import com.test.security.social.support.SocialUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;


@RestController
public class AppSecurityController extends SocialController {

	@Autowired
	private ProviderSignInUtils providerSignInUtils;

	@Autowired
	private AppSingUpUtils appSingUpUtils;

    @Autowired
    TokenEndpoint tokenEndpoint;

    /**
     * 覆盖默认的方法，格式化响应
     *
     * @param principal
     * @param parameters
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/oauth/token", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) {
        ResponseEntity<OAuth2AccessToken> result;
        String message;
        try {
            result = tokenEndpoint.postAccessToken(principal, parameters);
        } catch (Exception e) {
            String error = null;
            if (e instanceof InvalidGrantException) {
                if ("坏的凭证".equals(e.getMessage())) {
                    error = "用户名或密码信息错误";
                }
            }
            message = error != null ? error: e.getMessage();

            ResponseData response = new ResponseData()
                    .setCode(HttpStatus.UNAUTHORIZED.value())
                    .setMessage(message);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
        message = "refresh_token".equals(parameters.get("grant_type"))
                ? "刷新token成功"
                : "获取token成功";
        return ResponseEntity
                .status(result.getStatusCodeValue())
                .headers(result.getHeaders())
                .body(new ResponseData().setMessage(message).setData(result.getBody()));
    }

	/**
     * 需要引导用户注册或绑定时，通过此服务获取当前社交用户的信息
	 * 返回401（表示认证失败，第一次登陆）和用户信息
	 * @param request
	 * @return
	 */
	@GetMapping(SocialConstants.DEFAULT_CURRENT_SOCIAL_USER_INFO_URL)
	public ResponseEntity getSocialUserInfo(HttpServletRequest request) {
	    // 从请求中拿用户信息
		Connection<?> connection = providerSignInUtils.getConnectionFromSession(new ServletWebRequest(request));
		// 用户信息存储到redis
		appSingUpUtils.saveConnectionData(new ServletWebRequest(request), connection.createData());
		// 构建用户信息
        SocialUserInfo socialUserInfo = buildSocialUserInfo(connection);

        ResponseData response = new ResponseData()
                .setCode(HttpStatus.UNAUTHORIZED.value())
                .setMessage("第一次登录需要绑定账号，轻在30分钟内完成注册绑定")
                .setData(socialUserInfo);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

}
