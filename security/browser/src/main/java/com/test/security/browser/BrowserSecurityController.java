
package com.test.security.browser;


import com.test.security.base.ResponseData;
import com.test.security.social.SocialController;
import com.test.security.social.properties.SocialConstants;
import com.test.security.social.support.SocialUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 浏览器环境下与安全相关的服务
 *
 * @author Shoven
 * @since 2019-05-08 21:54
 */
@RestController
public class BrowserSecurityController extends SocialController {

	@Autowired
	private ProviderSignInUtils providerSignInUtils;

	/**
	 * 需要引导用户注册或绑定时，通过此服务获取当前社交用户的信息
	 * 返回401（表示认证失败，第一次登陆）和用户信息
     *
	 * @param request
	 * @return
	 */
	@GetMapping(SocialConstants.DEFAULT_CURRENT_SOCIAL_USER_INFO_URL)
	public ResponseEntity getSocialUserInfo(HttpServletRequest request) {
		Connection<?> connection = providerSignInUtils.getConnectionFromSession(new ServletWebRequest(request));
        SocialUserInfo socialUserInfo = buildSocialUserInfo(connection);
        ResponseData response = new ResponseData()
                .setCode(HttpStatus.UNAUTHORIZED.value())
                .setMessage("第一次登录需要绑定账号")
                .setData(socialUserInfo);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}
}
