package com.github.shawven.calf.examples.oauth2.service;



import com.github.shawven.calf.examples.oauth2.domain.RbacRole;
import com.github.shawven.calf.examples.oauth2.domain.RbacRolePermissionDTO;

import java.util.List;

/**
 * <p>
 * 权限表 分菜单权限、资源权限；
菜单权限：前端按钮展示，资源权限：API请求；
取得一个菜单权限是，会拥有下级的菜单权限和资源权限； 服务类
 * </p>
 *
 * @author Generator
 * @date 2020-03-02
 */
public interface RbacPermissionService {

    /**
     * 添加菜单权限到角色
     *
     * @param roles 待添加权限的角色列表
     * @return 已添加权限的角色列表
     */
    List<RbacRolePermissionDTO> addMenuPermissionToRole(List<RbacRole> roles);

    /**
     * 添加API权限到角色
     *
     * @param roles 待添加权限的角色列表
     * @return 已添加权限的角色列表
     */
    List<RbacRolePermissionDTO> addApiPermissionToRole(List<RbacRole> roles);
}
