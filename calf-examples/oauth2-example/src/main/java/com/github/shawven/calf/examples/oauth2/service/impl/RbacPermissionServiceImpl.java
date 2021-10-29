package com.github.shawven.calf.examples.oauth2.service.impl;

import com.github.shawven.calf.examples.oauth2.domain.RbacPermission;
import com.github.shawven.calf.examples.oauth2.domain.RbacRole;
import com.github.shawven.calf.examples.oauth2.domain.RbacRolePermissionDTO;
import com.github.shawven.calf.examples.oauth2.service.RbacPermissionService;
import com.github.shawven.calf.examples.oauth2.support.NodeTree;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Generator
 * @date 2020-03-02
 */
@Service
public class RbacPermissionServiceImpl implements RbacPermissionService {

    @Override
    public List<RbacRolePermissionDTO> addMenuPermissionToRole(List<RbacRole> roles) {
        return addPermissionToRole(roles, false);
    }

    @Override
    public List<RbacRolePermissionDTO> addApiPermissionToRole(List<RbacRole> roles) {
        return addPermissionToRole(roles, true);
    }

    /**
     * 添加权限到角色
     *
     * @param roles 待添加权限的角色列表
     * @param isApiResource  是否API资源权限
     * @return 已添加权限的角色列表
     */
    private List<RbacRolePermissionDTO> addPermissionToRole(List<RbacRole> roles, boolean isApiResource) {
        if (roles.isEmpty()) {
            return emptyList();
        }
        // todo
        // 获取整个系统的权限表
        List<RbacPermission> permissions = Collections.emptyList();

        // 构造成权限节点树（含有菜单权限和API权限）
        List<PremissionNode> tree = NodeTree.<RbacPermission, PremissionNode>from(permissions)
                .rootFilter(permission -> permission.getParentId() == 0)
                .childFilter((parent, child) -> parent.getId().equals(child.getParentId()))
                .map(PremissionNode::form)
                .build();

        // 循环遍历角色，给每个角色赋予响应的权限
        return roles.stream().map(role -> {
            // 获取角色的权限ID集合
            String[] permissionIds = StringUtils.split(role.getPermissionIds(), ",");
            Set<RbacPermission> permission = new LinkedHashSet<>();

            if (ArrayUtils.isNotEmpty(permissionIds)) {
                // 遍历权限
                for (String permissionId : permissionIds) {
                    // 根据权限ID去权限节点树中找节点
                    PremissionNode findNode = NodeTree.findNode(tree,
                            item -> permissionId.equals(String.valueOf(item.getId())));
                    if (findNode == null) {
                        continue;
                    }
                    // 节点可能是一个叶子节点也可能是树
                    // 叶子节点：权限单纯只有一个访问路径
                    // 树节点: 比如菜单权限，一个菜单下会涉及多个资源权限
                    // 将整合树统一压扁成一维集合
                    List<PremissionNode> permissionNodeList = NodeTree.flatList(findNode);

                    // 再转成基本的权限对象
                    List<RbacPermission> findList = permissionNodeList.stream()
                            // 根据要求取菜单权限还是API权限
                            .filter(node -> node.getIsResource() == isApiResource)
                            .map(PremissionNode::to)
                            .distinct()
                            .collect(toList());
                    permission.addAll(findList);
                }
            }
            // 给某个角色赋予权限
            RbacRolePermissionDTO rolePermission = RbacRolePermissionDTO.from(role);
            rolePermission.setPermissions(new ArrayList<>(permission));
            return rolePermission;
        }).collect(toList());
    }

    /**
     * @author Shoven
     * @date 2019-11-07
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    static class PremissionNode extends RbacPermission implements NodeTree.Node<PremissionNode> {

        private static final long serialVersionUID = 7857795288425905496L;

        private List<PremissionNode> children;

        public static PremissionNode form(RbacPermission source) {
            PremissionNode target = new PremissionNode();
            BeanUtils.copyProperties(source, target);
            return target;
        }

        public static RbacPermission to(PremissionNode source) {
            RbacPermission target = new RbacPermission();
            BeanUtils.copyProperties(source, target);
            return target;
        }
    }
}
