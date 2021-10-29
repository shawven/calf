
package com.github.shawven.calf.examples.oauth2.support;


import com.github.shawven.calf.examples.oauth2.domain.User;
import com.github.shawven.calf.examples.oauth2.service.UserService;
import com.github.shawven.security.verification.security.PhoneUserDetailsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;


/**
 * security oauth2和社交登陆用户服务
 *
 * @author Shoven
 * @date 2019-04-22 17:35
 */
public class UserDetailsServiceImpl implements UserDetailsService, PhoneUserDetailsService, SocialUserDetailsService {

    private UserService userService;

    private AuthenticationManager manager;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
        manager = new AuthenticationManager();
    }

    /**
     * 根据"用户名"查找用户
     * 1. 第一次登陆时此处传入的是登陆接口的username字段
     * 2. 刷新token时，此处传入的是refresh_token中的principal，是用户ID
     *  而这个principal就是下方buildUserDetails返回的UserDetails接口实现的username    new SocialUser(user.getId()..
     *
     * @param principal
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        if (StringUtils.isBlank(principal)) {
            throw new UsernameNotFoundException("请输入用户名");
        }
        return manager.authenticate(principal);
    }


    /**
     * 根据手机号登陆查找用户
     *
     * @param principal
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByPhone(String principal) throws UsernameNotFoundException {
        if (StringUtils.isBlank(principal)) {
            throw new UsernameNotFoundException("请输入手机号");
        }
        return manager.authenticate(principal);
    }

    /**
     * 根据用户ID查找用户
     *
     * @param principal
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public SocialUserDetails loadUserByUserId(String principal) throws UsernameNotFoundException {
        if (NumberUtils.isParsable(principal)) {
            User user = userService.getById(Long.parseLong(principal));
            return buildUserDetails(user);
        }
        throw new UsernameNotFoundException("该用户不存在");
    }

    /**
     * 构建用户详情
     *
     * @param user
     * @return
     */
    private SocialUserDetails buildUserDetails(User user) {
        boolean enabled = !user.getIsDisabled();
        return new SocialUser(user.getId() + "", user.getPassword(),
                enabled, true, true, true,
                AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
    }


    private class AuthenticationManager {

        private Map<Method, Function<String, User>> functions;

        private Map<Method, List<Method>> methodGroup;

        /**
         * 检测输入类型，优先选择对应的方法
         * 字符串：使用用户名判断
         * 类手机号的数子：使用手机号、ID、用户名逐个判断
         * 非手机号数字：使用ID、用户名判断
         *
         * @return
         */
        public UserDetails authenticate(String principal) {
            // 根据输入类型选择一组优先级方法
            List<Method> methods = methodGroup.get(Method.of(principal));
            User user;
            for (Method method : methods) {
                // 逐个尝试
                if ((user = functions.get(method).apply(principal)) != null) {
                    return buildUserDetails(user);
                }
            }
            throw new UsernameNotFoundException("该用户不存在");
        }

        {
            // 优先级方法组
            methodGroup = new HashMap<>();
            methodGroup.put(Method.NUM, Arrays.asList(Method.NUM, Method.STRING));
            methodGroup.put(Method.PHONE_NUM, Arrays.asList(Method.PHONE_NUM, Method.NUM, Method.STRING));
            methodGroup.put(Method.STRING, Collections.singletonList(Method.STRING));

            // 实际对应的方法
            functions = new HashMap<>();
            functions.put(Method.NUM, s -> UserDetailsServiceImpl.this.userService.getById(Long.parseLong(s)));
            functions.put(Method.PHONE_NUM, s -> UserDetailsServiceImpl.this.userService.getByPhone(s));
            functions.put(Method.STRING, s -> UserDetailsServiceImpl.this.userService.getByUsername(s));
        }
    }

    private enum Method {
        //
        NUM,
        PHONE_NUM,
        STRING;
        private static Pattern pattern = Pattern.compile("[1]([3-9])[0-9]{9}");

        public static Method of(String principal) {
            // 非数字
            if (!NumberUtils.isParsable(principal)) {
                return STRING;
            } else
                // 数字属于手机号
                if (pattern.matcher(principal).matches()) {
                    return PHONE_NUM;
                } else {
                    return NUM;
                }
        }
    }
}

