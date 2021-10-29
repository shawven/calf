package com.github.shawven.calf.examples.oauth2.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Generator
 * @date 2019-11-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 姓名
     */
    private String fullName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户来源 1手动注册 2第三方登录
     */
    private Integer source;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户密码
     */
    @JsonIgnore
    private String password;

    /**
     * 密码错误次数
     */
    private Integer errorCount;

    /**
     * 选择账套ID
     */
    private Long chooseAccountId;

    /**
     * 选择企业ID
     */
    private Long chooseEnterpriseId;

    /**
     * 是否已付费用户
     */
    private Boolean isPaid;

    /**
     * 是否停用
     */
    private Boolean isDisabled;

    /**
     * 本次登陆时间
     */
    private Date loginTime;

    /**
     * 上次登陆时间
     */
    private Date lastLoginTime;

    /**
     * 本次登陆IP
     */
    private String loginIp;

    /**
     * 上次登陆IP
     */
    private String lastLoginIp;

    /**
     * 启用时间（管理员的软件使用期）
     */
    private Date enabledTime;

    /**
     * 失效时间（管理员的软件使用期）
     */
    private Date disabledTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
