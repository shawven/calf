package com.starter.demo.support.event;

import com.starter.demo.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;

/**
 * 登录成事件
 *
 * @author Shoven
 * @date 2019-11-10
 */
@Data
@AllArgsConstructor
public class LoginSuccessEvent {

    private User user;

    private HttpServletRequest request;
}
