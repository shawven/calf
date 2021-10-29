package com.github.shawven.calf.examples.oauth2.support.event;

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
