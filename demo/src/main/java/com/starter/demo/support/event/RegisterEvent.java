package com.starter.demo.support.event;

import com.starter.demo.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 注册事件
 *
 * @author Shoven
 * @date 2019-11-10
 */
@Data
@AllArgsConstructor
public class RegisterEvent {

    private User user;
}
