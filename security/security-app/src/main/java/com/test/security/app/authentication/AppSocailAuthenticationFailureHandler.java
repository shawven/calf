
package com.test.security.app.authentication;

import org.springframework.security.core.AuthenticationException;
import org.springframework.social.security.SocialAuthenticationRedirectException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社交登陆APP环境下认证失败处理器
 */
public class AppSocailAuthenticationFailureHandler extends AppAuthenticationFailureHandler {

	/**
	 *(non-Javadoc)
	 * @see org.springframework.security.web.authentication.AuthenticationFailureHandler#onAuthenticationFailure(HttpServletRequest, HttpServletResponse, AuthenticationException)
	 */
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException e) throws IOException {

        if (e instanceof SocialAuthenticationRedirectException) {
            response.sendRedirect(((SocialAuthenticationRedirectException) e).getRedirectUrl());
            return;
        }

        super.onAuthenticationFailure(request, response, e);
	}
}
