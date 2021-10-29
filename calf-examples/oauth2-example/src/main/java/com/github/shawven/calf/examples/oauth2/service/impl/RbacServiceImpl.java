package com.github.shawven.calf.examples.oauth2.service.impl;

import com.github.shawven.calf.examples.oauth2.domain.RbacPermission;
import com.github.shawven.calf.examples.oauth2.domain.RbacPermissionDTO;
import com.github.shawven.calf.examples.oauth2.domain.RbacRole;
import com.github.shawven.calf.examples.oauth2.domain.RbacRolePermissionDTO;
import com.github.shawven.calf.examples.oauth2.service.AopSupport;
import com.github.shawven.calf.examples.oauth2.service.RbacPermissionService;
import com.github.shawven.calf.examples.oauth2.service.RbacService;
import com.github.shawven.calf.examples.oauth2.support.CtxData;
import com.github.shawven.calf.examples.oauth2.support.CtxDataAccessor;
import com.github.shawven.calf.examples.oauth2.support.NodeTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

/**
 * @author Shoven
 * @date 2019-11-07
 */
@Slf4j
@Service("rbacService")
public class RbacServiceImpl implements RbacService, AopSupport {

    @Autowired
    private CtxDataAccessor ctxDataAccessor;

    @Autowired
    private RbacPermissionService rbacPermissionService;

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${app.allow-public-api-request}")
    private boolean allowAccessPublicApi;

    @Override
    public List<RbacRolePermissionDTO> listEnabledRolesForDecision(Long userId, Long enterpriseId, Long accountId) {
        // todo
        List<RbacRole> roles = Collections.emptyList();
        return rbacPermissionService.addApiPermissionToRole(roles);
    }

    @Override
    public List<RbacPermissionDTO> listGrantedPermissionsForSystem(Long userId, Long enterpriseId, Long accountId) {
        // todo
        List<RbacPermission> permissions=  Collections.emptyList();
        return NodeTree.<RbacPermission, RbacPermissionDTO>from(permissions)
                .rootFilter(item -> item.getParentId().equals(0))
                .childFilter((parent, child) -> child.getParentId().equals(parent.getId()))
                .map(RbacPermissionDTO::form)
                .build();
    }

    @Override
    public List<RbacPermissionDTO> listPermissionsForSystem() {
        // todo
        List<RbacPermission> permissions = Collections.emptyList();
        return NodeTree.<RbacPermission, RbacPermissionDTO>from(permissions)
                .rootFilter(item -> item.getParentId().equals(0))
                .childFilter((parent, child) -> child.getParentId().equals(parent.getId()))
                .map(RbacPermissionDTO::form)
                .build();
    }


    @Override
    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
        // 匿名用户、或者未认证直接拒绝
        if (authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return false;
        }
        CtxData ctxData;
        try {
            ctxData = ctxDataAccessor.getContextDataFromContext();
        } catch (Exception e){
            ctxDataAccessor.release();
            throw e;
        }
        // 匿名用户直接拒绝
        if (ctxData.isAnonymous()) {
            return false;
        }
        // 管理员允许全部权限
        if (ctxData.isAdmin()) {
            return true;
        }

        String accessUrl = request.getRequestURI();
        // 得到用户权限ID集合
        List<Integer> permissionIds = ctxData.getPermissions();
        Set<Integer> ownIds = permissionIds == null ? emptySet() : new HashSet<>(permissionIds);

        // 得到需要权限ID集合
        List<Integer> needIds = getRequiredPermissionIds(accessUrl);

        // 属于公共权限（没有配置到数据库）
        if (needIds.isEmpty()) {
            return allowAccessPublicApi;
        }

        // 需要权限校验
        for (Integer needId : needIds) {
            if (ownIds.contains(needId)) {
                return true;
            }
        }
        log.error("用户[{}]越权访问[{}]被拒绝", ctxData.getUserId(), accessUrl);
        return false;
    }

    /**
     * 获取需要的权限ID集合
     *
     * @param accessUrl
     * @return
     */
    private List<Integer> getRequiredPermissionIds(String accessUrl) {
        // todo
        Map<String, List<Integer>> pathToIds = emptyMap();
        return pathToIds.entrySet().stream()
                .filter(entry -> pathMatcher.match(entry.getKey(), accessUrl))
                .flatMap(entry  -> entry.getValue().stream())
                .collect(toList());
    }
}
