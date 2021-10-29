package com.github.shawven.calf.examples.oauth2.domain;

import com.github.shawven.calf.examples.oauth2.support.NodeTree;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限菜单
 *
 * @author Shoven
 * @date 2019-11-07
 */
@Data
public class RbacPermissionDTO implements NodeTree.Node<RbacPermissionDTO> {

    private Integer id;

    private String name;

    private String label;

    private String path;

    private static final long serialVersionUID = 7857795288425905496L;

    private List<RbacPermissionDTO> children = new ArrayList<>();

    public static RbacPermissionDTO form(RbacPermission source) {
        RbacPermissionDTO target = new RbacPermissionDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

}
