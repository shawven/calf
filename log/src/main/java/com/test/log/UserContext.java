package com.test.log;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Shoven
 * @date 2019-07-05 14:22
 */
public class UserContext {

    public static UserContext get() {
        HttpSession session = getRequest().getSession();
        return new UserContext();
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    public static void setAttribute(String key, Object value) {
        HttpSession session = getRequest().getSession();
        session.setAttribute(key, value);
    }
}
