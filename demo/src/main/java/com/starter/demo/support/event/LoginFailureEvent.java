package com.starter.demo.support.event;

import com.starter.demo.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;

/**
 * 登录失败事件
 *
 * @author Shoven
 * @date 2019-11-10
 */
@Data
@AllArgsConstructor
public class LoginFailureEvent {

    private String principal;

    private Exception e;

    /**
     * 登录成事件
     *
     * @author Shoven
     * @date 2019-11-10
     */
    @Data
    @AllArgsConstructor
    public static class LoginSuccessEvent {

        private User user;

        private HttpServletRequest request;
    }
}
