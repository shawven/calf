package com.github.shawven.calf.examples.oauth2.mapper;


import com.github.shawven.calf.examples.oauth2.domain.RbacPermission;
import com.github.shawven.calf.examples.oauth2.mapper.base.BaseMapper;

import java.util.List;

/**
 * <p>
 * 权限表 分菜单权限、资源权限；
菜单权限：前端按钮展示，资源权限：API请求；
取得一个菜单权限是，会拥有下级的菜单权限和资源权限； Mapper 接口
 * </p>
 *
 * @author Generator
 * @date 2020-03-02
 */
public interface RbacPermissionMapper extends BaseMapper<RbacPermission> {

    /**
     * 查询所有的权限
     *
     * @return
     */
    List<RbacPermission> selectAll();
}
