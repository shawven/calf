package com.github.shawven.calf.examples.oauth2.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 权限表 分菜单权限、资源权限；
 * 菜单权限：前端按钮展示，资源权限：API请求；
 * 取得一个菜单权限是，会拥有下级的菜单权限和资源权限；
 * </p>
 *
 * @author Generator
 * @date 2019-11-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("rbac_permission")
public class RbacPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 标签
     */
    private String label;

    /**
     * 名称
     */
    private String name;

    /**
     * 路径
     */
    private String path;

    /**
     * 父ID
     */
    private Integer parentId;

    /**
     * 是否资源权限（0菜单权限 1api接口权限 ）
     */
    private Boolean isResource;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
