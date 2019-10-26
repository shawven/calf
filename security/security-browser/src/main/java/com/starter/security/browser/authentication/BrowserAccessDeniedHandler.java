package com.starter.security.browser.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.security.base.ResponseData;
import com.starter.security.base.ResponseType;
import com.starter.security.browser.properties.BrowserProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 非匿名用户且没有记住我，验证是失败时会走这里
 *
 * @author Shoven
 * @date 2018/11/1 18:15
 * 请求拒绝，没有权限
 */
public class BrowserAccessDeniedHandler extends AccessDeniedHandlerImpl {

    private BrowserProperties browserProperties;

    private ObjectMapper objectMapper;

    public BrowserAccessDeniedHandler(BrowserProperties browserProperties) {
        this.browserProperties = browserProperties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException, ServletException {
        if (ResponseType.JSON.equals(browserProperties.getResponseType())) {
            int status = HttpStatus.FORBIDDEN.value();
            ResponseData rsp = new ResponseData()
                    .setCode(status)
                    .setMessage(HttpStatus.FORBIDDEN.getReasonPhrase());

            response.setCharacterEncoding("UTF-8");
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(rsp));
        } else {
            super.handle(request, response, e);
        }
    }
}
