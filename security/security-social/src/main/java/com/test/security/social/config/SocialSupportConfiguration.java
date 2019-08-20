package com.test.security.social.config;

import com.test.security.social.DefaultSocialUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.security.SocialUserDetailsService;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Configuration
public class SocialSupportConfiguration {

    /**
     * 默认认证器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public SocialUserDetailsService socialUserDetailsService() {
        return new DefaultSocialUserDetailsService();
    }
}
