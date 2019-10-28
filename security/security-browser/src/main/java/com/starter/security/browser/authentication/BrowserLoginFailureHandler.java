package com.starter.security.browser.authentication;

import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Shoven
 * @date 2019-10-28
 */
public interface BrowserLoginFailureHandler {

    void onAuthenticationFailure(HttpServletRequest request,
                                 HttpServletResponse response,
                                 AuthenticationException exception) throws IOException, ServletException;
}
