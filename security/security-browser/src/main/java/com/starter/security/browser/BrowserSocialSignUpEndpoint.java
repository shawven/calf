
package com.starter.security.browser;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.security.base.ResponseData;
import com.starter.security.base.ResponseType;
import com.starter.security.browser.properties.BrowserProperties;
import com.starter.security.social.SocialInfoMethod;
import com.starter.security.social.properties.SocialConstants;
import com.starter.security.social.support.SocialUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.social.security.SocialAuthenticationRedirectException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器环境下与安全相关的服务
 *
 * @author Shoven
 * @since 2019-05-08 21:54
 */
@Controller
public class BrowserSocialSignUpEndpoint extends SocialInfoMethod {

    private static final Logger logger = LoggerFactory.getLogger(BrowserSocialSignUpEndpoint.class);

	@Autowired
	private ProviderSignInUtils providerSignInUtils;

	@Autowired
    private BrowserProperties browserProperties;

	/**
	 * 需要引导用户注册或绑定时，通过此服务获取当前社交用户的信息
	 * 返回401（表示认证失败，第一次登陆）和用户信息
     *
	 * @param request
	 * @return
	 */
	@GetMapping(SocialConstants.DEFAULT_CURRENT_SOCIAL_USER_INFO_URL)
	public void getSocialUserInfo(HttpServletRequest request, HttpServletResponse response) {
		Connection<?> connection = providerSignInUtils.getConnectionFromSession(new ServletWebRequest(request));
        SocialUserInfo socialUserInfo = buildSocialUserInfo(connection);

        if (ResponseType.JSON.equals(browserProperties.getResponseType())) {
            ResponseData result = new ResponseData()
                    .setCode(HttpStatus.UNAUTHORIZED.value())
                    .setMessage("第一次登录需要绑定账号")
                    .setData(socialUserInfo);

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            try {
                response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            request.setAttribute("socialUserInfo", "socialUserInfo");
            try {
                response.sendRedirect(browserProperties.getSignUpUrl());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
	}
}
