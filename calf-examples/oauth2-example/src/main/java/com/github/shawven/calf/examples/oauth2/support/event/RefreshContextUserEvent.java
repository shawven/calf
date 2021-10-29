package com.github.shawven.calf.examples.oauth2.support.event;

import com.github.shawven.calf.examples.oauth2.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 刷新上下文用户事件
 *
 * @author Shoven
 * @date 2019-11-10
 */
@Data
@AllArgsConstructor
public class RefreshContextUserEvent {

    private User user;
}
