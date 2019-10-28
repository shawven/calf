package com.starter.security.social;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Shoven
 * @since 2019-04-23 15:51
 */
public interface PhoneUserDetailsService {


    /**
     * 根据手机号登陆
     *
     * @param phone
     * @return
     * @throws UsernameNotFoundException
     */
    UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException;
}
