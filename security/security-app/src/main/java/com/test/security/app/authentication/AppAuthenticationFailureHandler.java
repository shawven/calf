
package com.test.security.app.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.security.base.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * APP环境下认证失败处理器
 */
public class AppAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 *(non-Javadoc)
	 * @see org.springframework.security.web.authentication.AuthenticationFailureHandler#onAuthenticationFailure(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)
	 */
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException e) throws IOException {
        int status = HttpStatus.BAD_REQUEST.value();
        ResponseData newResponse = new ResponseData()
                .setCode(status)
                .setMessage(e.getMessage());

        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(newResponse));
	}
}
