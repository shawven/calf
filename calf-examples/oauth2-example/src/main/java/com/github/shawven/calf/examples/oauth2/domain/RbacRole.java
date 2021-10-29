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
 * 角色表（一般用户创建的角色）
 * </p>
 *
 * @author Generator
 * @date 2019-11-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("rbac_role")
public class RbacRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色所属企业ID
     */
    private Long enterpriseId;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 权限id集合
     */
    private String permissionIds;

    /**
     * 是否禁用
     */
    private Boolean isDisabled;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
