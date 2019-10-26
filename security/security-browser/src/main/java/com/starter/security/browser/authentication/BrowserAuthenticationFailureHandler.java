
package com.starter.security.browser.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.security.base.ResponseData;
import com.starter.security.base.ResponseType;
import com.starter.security.browser.properties.BrowserProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器环境下登录失败的处理器
 *
 * @author Shoven
 * @since 2019-05-08 21:55
 */
public class BrowserAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private ObjectMapper objectMapper;

	private BrowserProperties browserProperties;

    public BrowserAuthenticationFailureHandler(BrowserProperties browserProperties) {
        this.objectMapper = new ObjectMapper();
        this.browserProperties = browserProperties;
    }

    @Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		if (ResponseType.JSON.equals(browserProperties.getResponseType())) {
            int status = HttpStatus.UNAUTHORIZED.value();
            ResponseData result = new ResponseData()
                    .setCode(status)
                    .setMessage(exception.getMessage());

            response.setStatus(status);
			response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(result));
		}else{
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}
