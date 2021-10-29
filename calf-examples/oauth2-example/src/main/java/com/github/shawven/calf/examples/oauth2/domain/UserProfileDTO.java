package com.github.shawven.calf.examples.oauth2.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Shoven
 * @date 2019-11-15
 */
@Data
@Accessors(chain = true)
public class UserProfileDTO implements Serializable {

    private static final long serialVersionUID = -7994849178745278243L;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 显示名称
     */
    private String displayName;

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
     * 手机号
     */
    private String phone;

    /**
     * 是否管理员
     */
    private boolean admin;

    /**
     * 是否绑定手机号
     */
    private boolean bindPhone;

    /**
     * 企业的账套数
     */
    private Integer ownAccountNum;

    /**
     * 可用的账套数
     */
    private Integer relatedAccountNum;

    /**
     * 创建时间
     */
    private Date createTime;

    public UserProfileDTO(User user, boolean isAdmin) {
        this.userId = user.getId();
        this.displayName = StringUtils.isNotBlank(user.getNickname()) ? user.getNickname() : user.getUsername();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.fullName = user.getFullName();
        this.avatar = user.getAvatar();
        this.phone = user.getPhone();
        this.admin = isAdmin;
        this.bindPhone = StringUtils.isNotBlank(user.getPhone());
        this.createTime = user.getCreateTime();
    }

}
