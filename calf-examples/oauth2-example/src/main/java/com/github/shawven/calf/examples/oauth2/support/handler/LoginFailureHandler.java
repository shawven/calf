package com.github.shawven.calf.examples.oauth2.support.handler;

import com.github.shawven.security.app.AppLoginFailureHandler;
import com.github.shawven.security.verification.VerificationConstants;
import com.github.shawven.calf.examples.oauth2.support.event.LoginFailureEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Shoven
 * @date 2019-10-28
 */
public class LoginFailureHandler implements AppLoginFailureHandler {

    private ApplicationEventPublisher eventPublisher;

    public LoginFailureHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        Exception e)  {
        if (e instanceof BadCredentialsException
                || e instanceof InvalidGrantException) {
            String username = request.getParameter("username");
            if (StringUtils.isBlank(username)) {
                username = request.getParameter(VerificationConstants.PHONE_PARAMETER_NAME);
            }
            eventPublisher.publishEvent(new LoginFailureEvent(username, e));
        }
    }
}
