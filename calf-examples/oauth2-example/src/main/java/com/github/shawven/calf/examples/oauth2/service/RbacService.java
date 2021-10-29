package com.github.shawven.calf.examples.oauth2.service;

import com.github.shawven.calf.examples.oauth2.domain.RbacPermissionDTO;
import com.github.shawven.calf.examples.oauth2.domain.RbacRolePermissionDTO;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Shoven
 * @date 2019-11-06
 */
public interface RbacService {

    /**
     * 列出决策用途已启用的角色
     *
     * @param userId
     * @param enterpriseId
     * @param accountId
     * @return 带权限信息的角色列表
     */
    List<RbacRolePermissionDTO> listEnabledRolesForDecision(Long userId, Long enterpriseId, Long accountId);

    /**
     * 列出系统的账套下该用户被授予的权限
     *
     * @param userId
     * @param enterpriseId
     * @param accountId
     * @return
     */
    List<RbacPermissionDTO> listGrantedPermissionsForSystem(Long userId, Long enterpriseId, Long accountId);

    /**
     * 列出系统所有的权限
     *
     * @return
     */
    List<RbacPermissionDTO> listPermissionsForSystem();

    /**
     * 判断当前请求是否有权限访问
     *
     * @param request 请求
     * @param authentication 身份对象
     * @return 是否通过
     */
    boolean hasPermission(HttpServletRequest request, Authentication authentication);

}
