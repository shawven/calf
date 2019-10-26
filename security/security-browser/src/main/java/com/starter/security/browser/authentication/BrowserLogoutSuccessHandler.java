
package com.starter.security.browser.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.security.base.ResponseData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认的退出成功处理器，如果设置了app.security.browser.signOutUrl，则跳到配置的地址上，
 * 如果没配置，则返回json格式的响应。
 *
 * @author Shoven
 * @since 2019-05-08 21:54
 */
public class BrowserLogoutSuccessHandler implements LogoutSuccessHandler {

	public BrowserLogoutSuccessHandler(String signOutSuccessUrl) {
		this.signOutSuccessUrl = signOutSuccessUrl;
	}

	private String signOutSuccessUrl;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException {
		if (StringUtils.isBlank(signOutSuccessUrl)) {
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(objectMapper.writeValueAsString(new ResponseData("退出成功")));
		} else {
			response.sendRedirect(signOutSuccessUrl);
		}

	}

}
