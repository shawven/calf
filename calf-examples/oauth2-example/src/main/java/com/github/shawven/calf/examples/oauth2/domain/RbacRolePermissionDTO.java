package com.github.shawven.calf.examples.oauth2.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Shoven
 * @date 2019-11-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RbacRolePermissionDTO extends RbacRole {

    private static final long serialVersionUID = -9080995496841767577L;

    private List<RbacPermission> permissions = Collections.emptyList();

    public static RbacRolePermissionDTO from(RbacRole source) {
        RbacRolePermissionDTO target = new RbacRolePermissionDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
