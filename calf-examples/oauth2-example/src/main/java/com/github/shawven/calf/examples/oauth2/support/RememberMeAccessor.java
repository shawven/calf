package com.github.shawven.calf.examples.oauth2.support;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Shoven
 * @date 2020-01-06
 */
@Component
public class RememberMeAccessor {

    @Autowired
    private Context context;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查是否已记住当前请求
     *
     * @param request Http请求对象
     * @return 是否记住
     */
    public int isRememberMe(HttpServletRequest request) {
        if (invalidTimestamp(request.getParameter("t"))) {
            return 1;
        }
        Boolean hasKey = redisTemplate.hasKey(getRequestMd5String(request));
        return hasKey!= null && hasKey ? 2 : 0;
    }

    /**
     * 记住当前请求
     *
     * @param request Http请求对象
     */
    public void remember(HttpServletRequest request) {
        redisTemplate.opsForValue().set(getRequestMd5String(request), "", 1, TimeUnit.MINUTES);
    }

    /**
     * 无效的毫秒数
     *
     * @param millStr 毫秒时间戳
     * @return
     */
    private boolean invalidTimestamp(String millStr) {
        long mills;
        try {
            mills = Long.parseLong(millStr);
        } catch (NumberFormatException e) {
            return true;
        }
        return (System.currentTimeMillis() - Long.parseLong(millStr)) / 1000 > 60;
    }

    /**
     * 请求md5值
     *
     * @param request Http请求对象
     * @return md5值
     */
    private String getRequestMd5String(HttpServletRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        request.getParameterMap().forEach(builder::queryParam);
        String url = builder.build().toUri().toString();
        Object uuid = context.getUserId();
        if (uuid == null) {
            uuid = request.getRequestedSessionId();
        }
        return DigestUtils.md5Hex(geRequestKey(uuid) + url).toLowerCase();
    }

    /**
     * 获取api请求redis存储Key
     *
     * @param userId 用户ID
     * @return redis Key
     */
    private String geRequestKey(Object userId) {
        return "user::request:" + userId.toString();
    }
}
