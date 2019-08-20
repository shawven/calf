
package com.test.security.browser.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.security.browser.property.BrowserProperties;
import com.test.security.base.ResponseType;
import com.test.security.base.ResponseData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器环境下登录成功的处理器
 *
 * @author Shoven
 * @since 2019-05-08 21:55
 */
public class BrowserAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private ObjectMapper objectMapper;

	private BrowserProperties browserProperties;

	private RequestCache requestCache;

    private LoginSuccessHandler loginSuccessHandler;

    public BrowserAuthenticationSuccessHandler(BrowserProperties browserProperties,
                                               LoginSuccessHandler loginSuccessHandler) {
        this.objectMapper = new ObjectMapper();
        this.requestCache = new HttpSessionRequestCache();
        this.browserProperties = browserProperties;
        this.loginSuccessHandler = loginSuccessHandler;
    }


	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
        if (loginSuccessHandler != null) {
            loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        }


		if (ResponseType.JSON.equals(browserProperties.getResponseType())) {
            ResponseData result = new ResponseData("登录成功");
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(HttpStatus.OK.value());
            response.getWriter().write(objectMapper.writeValueAsString(result));
		} else {
			// 如果设置了app.security.browser.singInSuccessUrl，总是跳到设置的地址上
			// 如果没设置，则尝试跳转到登录之前访问的地址上，如果登录前访问地址为空，则跳到网站根路径上
			if (StringUtils.isNotBlank(browserProperties.getSingInSuccessUrl())) {
				requestCache.removeRequest(request, response);
				setAlwaysUseDefaultTargetUrl(true);
				setDefaultTargetUrl(browserProperties.getSingInSuccessUrl());
			}
			super.onAuthenticationSuccess(request, response, authentication);
		}

	}

}
