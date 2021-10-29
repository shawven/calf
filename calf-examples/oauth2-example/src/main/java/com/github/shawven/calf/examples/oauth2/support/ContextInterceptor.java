package com.github.shawven.calf.examples.oauth2.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Shoven
 * @date 2019-11-14
 */
public class ContextInterceptor implements HandlerInterceptor {

    private CtxDataAccessor ctxDataAccessor;

    private RememberMeAccessor rememberMeAccessor;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String profile;

    public ContextInterceptor(CtxDataAccessor ctxDataAccessor,
                              RememberMeAccessor rememberMeAccessor,
                              String profile) {
        this.ctxDataAccessor = ctxDataAccessor;
        this.rememberMeAccessor = rememberMeAccessor;
        this.profile = profile;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!("local".equals(profile) || "dev".equals(profile))) {
            int result = rememberMeAccessor.isRememberMe(request);
            if (result == 1) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json,charset=utf-8");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                Response body = Response.badRequest("无效的请求").getBody();
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return false;
            }
            if (result == 2) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json,charset=utf-8");
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                Response body = Response.unprocesable("重复的请求").getBody();
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return false;
            }
            rememberMeAccessor.remember(request);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ctxDataAccessor.release();
    }
}
