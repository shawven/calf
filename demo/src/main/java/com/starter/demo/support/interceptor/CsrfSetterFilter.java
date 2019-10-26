package com.starter.demo.support.interceptor;

import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Shoven
 * @date 2019-07-11 18:32
 */
public class CsrfSetterFilter implements HandlerInterceptor {
    private static final String DEFAULT_CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class.getName().concat(".CSRF_TOKEN");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
