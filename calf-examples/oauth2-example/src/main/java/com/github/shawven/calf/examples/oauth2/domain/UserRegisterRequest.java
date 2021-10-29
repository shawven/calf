package com.github.shawven.calf.examples.oauth2.domain;

import com.github.shawven.calf.examples.oauth2.support.exception.ArgumentException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * 注册请求
 *
 * @author Shoven
 * @date 2019-11-15
 */
@Data
@Accessors(chain = true)
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -869139059950679803L;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户名
     */
    private String username;

    /**
     * 姓名
     */
    private String fullName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像
     */
    private String avatar;

    public void validate() {
        if (StringUtils.isNotBlank(phone)) {
            // 验证格式
            if (!Pattern.matches("1[3-9][0-9]{9}", phone)) {
                throw new ArgumentException("手机号格式不正确");
            }
        } else {
            // 手机号为空的同时用户名不允许为空
            if (StringUtils.isBlank(username)) {
                throw new ArgumentException("手机号和用户名不能同时为空");
            }
        }
        if (StringUtils.isBlank(password)) {
            throw new ArgumentException("未填写密码");
        }
    }

    public User newUser() {
        if (StringUtils.isNotBlank(phone)) {
            // 有手机号没用户名，用户名设置为手机号
            if (StringUtils.isBlank(username)) {
                username = "wq_" + phone;
            }
            if (StringUtils.isBlank(nickname)) {
                nickname = "m_" + phone;
            }
        }
        return new User()
            .setUsername(username)
            .setNickname(nickname)
            .setFullName(fullName)
            .setAvatar(avatar)
            .setPhone(phone)
            .setPassword(password);
    }
}
