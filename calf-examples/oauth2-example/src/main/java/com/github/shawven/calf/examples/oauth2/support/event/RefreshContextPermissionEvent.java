package com.github.shawven.calf.examples.oauth2.support.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 刷新上下文权限事件
 *
 * @author Shoven
 * @date 2019-11-10
 */
@Data
@AllArgsConstructor
public class RefreshContextPermissionEvent {

    private Long userId;

    private List<Integer> permissionIds;
}
