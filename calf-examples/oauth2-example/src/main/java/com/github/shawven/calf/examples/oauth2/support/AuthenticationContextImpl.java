package com.github.shawven.calf.examples.oauth2.support;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Shoven
 * @date 2020-03-04
 */
@Component
public class AuthenticationContextImpl implements AuthenticationContext {
    /**
     * 匿名用户名称
     */
    private static final String ANONYMOUS_USER = "anonymousUser";

    private static Gson gson = new Gson();

    /**
     * 获取用户ID
     *
     * @return
     */
    @Override
    public Long getUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0L;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            String username = (String) principal;
            if (ANONYMOUS_USER.equals(username)) {
                return 0L;
            }
            return Long.parseLong(username);
        } else if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            if (NumberUtils.isParsable(username)) {
                return Long.parseLong(((UserDetails) principal).getUsername());
            }
            return 0L;
        } else {
            return 0L;
        }
    }

    /**
     * 获取客户端ID
     *
     * @return
     */
    @Override
    public String getClientId() {
        Authentication authentication = getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            // 已经登录成功时
            return ((OAuth2Authentication)authentication).getOAuth2Request().getClientId();
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            // 首次登录成功时
            return ((UserDetails)authentication.getPrincipal()).getUsername();
        } else {
            throw new IllegalStateException("无法获取client信息");
        }
    }

    /**
     * 获取tokenID
     *
     * @return
     */
    @Override
    public String getSessionId() {
        Authentication authentication = getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
            String tokenValue = details.getTokenValue();
            String claims = JwtHelper.decode(tokenValue).getClaims();
            Map<String, Object> map = gson.fromJson(claims, new TypeToken<Map<String, Object>>(){}.getType());
            return (String) map.get("jti");
        }
        throw new IllegalStateException("无法获取Token信息");
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
