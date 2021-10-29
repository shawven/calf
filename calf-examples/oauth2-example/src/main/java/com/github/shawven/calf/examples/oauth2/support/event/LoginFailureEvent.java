package com.github.shawven.calf.examples.oauth2.support.event;

import com.github.shawven.calf.examples.oauth2.domain.User;
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
