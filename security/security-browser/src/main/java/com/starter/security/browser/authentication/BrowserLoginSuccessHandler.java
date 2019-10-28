package com.starter.security.browser.authentication;

import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Shoven
 * @since 2019-05-10 18:01
 */
public interface BrowserLoginSuccessHandler {

    void onAuthenticationSuccess(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Authentication authentication) throws IOException, ServletException;
}
