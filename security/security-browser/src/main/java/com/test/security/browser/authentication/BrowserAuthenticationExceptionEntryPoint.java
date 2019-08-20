package com.test.security.browser.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.security.browser.property.BrowserProperties;
import com.test.security.core.ResponseType;
import com.test.security.core.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Shoven
 * @date 2018/11/2 10:48
 */

public class BrowserAuthenticationExceptionEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private static final String UNAUTHORIZED = "Full authentication is required to access this resource";

    private static final String REQUIRE_LOGIN = "Require login";

    private BrowserProperties browserProperties;

    private ObjectMapper objectMapper;

    public BrowserAuthenticationExceptionEntryPoint(BrowserProperties browserProperties) {
        super(browserProperties.getSignInUrl());
        this.objectMapper = new ObjectMapper();
        this.browserProperties = browserProperties;
    }


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException {
        if (ResponseType.JSON.equals(browserProperties.getResponseType())) {
            String errorMessage = UNAUTHORIZED.equals(e.getMessage())
                    ? REQUIRE_LOGIN
                    : HttpStatus.UNAUTHORIZED.getReasonPhrase();

            int status = HttpStatus.UNAUTHORIZED.value();
            ResponseData rsp = new ResponseData()
                    .setMessage(errorMessage)
                    .setCode(status);

            response.setCharacterEncoding("UTF-8");
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(rsp));
        } else {
            super.commence(request, response, e);
        }
    }
}
