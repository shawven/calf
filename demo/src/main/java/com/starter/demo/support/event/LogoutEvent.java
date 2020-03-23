package com.starter.demo.support.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Shoven
 * @date 2020-02-27
 */
@Data
@AllArgsConstructor
public class LogoutEvent {

    private Long userId;
}
