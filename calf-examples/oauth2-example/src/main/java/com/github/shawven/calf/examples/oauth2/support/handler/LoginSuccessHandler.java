package com.github.shawven.calf.examples.oauth2.support.handler;

import com.github.shawven.calf.examples.oauth2.domain.User;
import com.github.shawven.calf.examples.oauth2.service.UserService;
import com.github.shawven.security.app.AppLoginSuccessHandler;
import com.github.shawven.calf.examples.oauth2.support.event.LoginFailureEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Shoven
 * @date 2019-10-28
 */
public class LoginSuccessHandler implements AppLoginSuccessHandler {

    private ApplicationEventPublisher eventPublisher;

    private UserService userService;

    public LoginSuccessHandler(ApplicationEventPublisher eventPublisher, UserService userService) {
        this.eventPublisher = eventPublisher;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {
        String userId;
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            userId = (String) principal;
        } else {
            UserDetails userDetails = (UserDetails) principal;
            userId = userDetails.getUsername();
        }
        User user = userService.getById(Long.parseLong(userId));
        eventPublisher.publishEvent(new LoginFailureEvent.LoginSuccessEvent(user, request));
    }
}
