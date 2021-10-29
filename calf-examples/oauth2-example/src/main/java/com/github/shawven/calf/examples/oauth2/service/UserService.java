package com.github.shawven.calf.examples.oauth2.service;

import com.github.shawven.calf.examples.oauth2.domain.User;
import com.github.shawven.calf.examples.oauth2.domain.UserProfileDTO;
import com.github.shawven.calf.examples.oauth2.domain.UserRegisterRequest;
import com.github.shawven.calf.examples.oauth2.domain.UserUpdateRequest;

import java.util.List;

/**
 * <p>
 * 操作员表 服务类
 * </p>
 *
 * @author Shoven
 * @date 2019-10-26
 */
public interface UserService {

    /**
     * 获取用户简介
     *
     * @param userId
     * @return
     */
    UserProfileDTO getUserProfile(Long userId);

    /**
     * 注册用户
     *
     * @param request
     * @return
     */
    User register(UserRegisterRequest request);


    /**
     * @param request
     */
    boolean updateUser(UserUpdateRequest request);

    /**
     * 修改手机号
     *
     * @param phone 手机号
     * @return
     */
    boolean updatePhone(Long userId, String phone);

    /**
     * 重设密码
     *
     *
     * @param phone
     * @param password 新密码
     * @return
     */
    boolean resetPassword(String phone, String password);

    /**
     * 刷新登陆记录
     *
     * @param user
     */
    boolean updateLoginRecord(User user);

    /**
     * 如果达到最大失败数禁止用户登录
     *
     * @param user 用户
     */
    void disabledIfReachTheMaxAttempts(User user);

    /**
     * 根据用户ID查找用户
     *
     * @param id
     * @return
     */
    User getById(Long id);

    /**
     * 根据手机号查找用户
     *
     * @param phone
     * @return
     */
    User getByPhone(String phone);

    /**
     * 根据用户名查找用户
     *
     * @return username
     */
    User getByUsername(String username);

    /**
     * 根据ID集合列出用户列表
     *
     * @param ids
     * @return
     */
    List<User> listUsersByIds(List<Long> ids);

    /**
     * 此手机号用户是否存在
     *
     * @param phone
     * @return
     */
    boolean existByPhone(String phone);

    /**
     * 此用户名是否存在
     *
     * @param phone
     * @return
     */
    boolean existByUsername(String phone);

    /**
     * 此邮箱是否存在
     * @param email
     * @return
     */
    boolean existByEmail(String email);

}
