package com.github.shawven.calf.examples.oauth2.support;

import com.github.shawven.security.oauth2.OAuth2Properties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author Shoven
 * @date 2020-01-06
 */
@Component
public class CtxDataAccessor {

    /**
     * 并发登录
     */
    private static final String CONCURRENT_LOGIN = "您的账号在已在其他地方登录";

    /**
     * 需要登录
     */
    private static final String REQUIRE_LOGIN = "用户身份已失效，请重新登录";

    /**
     * 当前线程持有的上下文数据
     */
    private static final ThreadLocal<CtxData> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * redis操作句柄
     */
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 认证上下文
     */
    private AuthenticationContext authenticationContext;

    private Map<String, Integer> clientExpireIn;

    public CtxDataAccessor(RedisTemplate<String, Object> redisTemplate,
                           AuthenticationContext authenticationContext,
                           OAuth2Properties auth2Properties) {
        this.redisTemplate = redisTemplate;
        this.authenticationContext = authenticationContext;
        this.clientExpireIn = auth2Properties.getClients().stream()
                .map(client -> Pair.of(client.getClientId(), client.getAccessTokenValidateSeconds()))
                .collect(Pair.toMap());
    }

    /**
     * 获取上下文数据
     *
     * @param userId 用户ID
     * @return
     */
    public CtxData getContextData(Long userId) {
        return getContextData(userId, true);
    }

    /**
     * 获取上下文数据
     *
     * @return
     */
    public CtxData getContextDataFromContext() {
        return getContextData(authenticationContext.getUserId(), true);
    }

    /**
     * 获取上下文数据
     *
     * @param userId 用户ID
     * @param checkConcurrency 检查用户登录并发
     * @return
     */
    public CtxData getContextData(Long userId, boolean checkConcurrency) {
        CtxData ctxData = THREAD_LOCAL.get();
        if (ctxData == null) {
            ctxData = (CtxData) redisTemplate.opsForValue().get(getContextKey(userId));
            if (ctxData != null) {
                if (checkConcurrency) {
                    checkState(ctxData);
                }
                ctxData.setCtxDataAccessor(this);
                THREAD_LOCAL.set(ctxData);
            }
        }
        if (ctxData == null) {
            // redis数据为空 强制重新登录
            throw new InsufficientAuthenticationException(REQUIRE_LOGIN);
        }
        return ctxData;
    }


    /**
     * 是否存在
     *
     * @param userId 用户Id
     * @return 是否存在
     */
    public boolean exist(Long userId) {
        String key = getContextKey(userId);
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }


    /**
     * 上下文持久化
     *
     * @param data ContextData
     */
    public void save(CtxData data) {
        // 缓存过期时间要大于token过期时间
        String contextKey = getContextKey(data.getUserId());
        int expireInForClient = getExpireInForClient();
        redisTemplate.opsForValue().set(contextKey, data, expireInForClient, TimeUnit.SECONDS);
        THREAD_LOCAL.set(data);
    }

    public void logout(Long userId) {
        redisTemplate.delete(getContextKey(userId));
    }

    /**
     * 释放当前线程的上下文
     */
    public void release() {
        THREAD_LOCAL.remove();
    }

    /**
     * 检查上下文数据状态，并发登录是以clientId为判定的，如：同时用web登录则为并发等，一边用web一边用H5则不算并发登录
     *
     * @param ctxData
     */
    private void checkState(CtxData ctxData) {
        // 获取客户端ID
        String clientId = authenticationContext.getClientId();
        String sessionId = authenticationContext.getSessionId();

        // 获取上下文session
        Map<String, String> session = ctxData.getClientSession();
        if (session != null) {
            String tokenIdInContext = session.get(clientId);
            // 存在不相等则为并发登录
            if (tokenIdInContext != null && !tokenIdInContext.equals(sessionId)) {
                // 并发登录
                throw new InsufficientAuthenticationException(CONCURRENT_LOGIN);
            }
        }
    }

    /**
     * 获取客户端的token过期时间
     *
     * @return
     */
    private int getExpireInForClient() {
        String clientId = authenticationContext.getClientId();
        // 配置的基础之上再加60秒，避免token马上过期时请求引发的问题
        return clientExpireIn.get(clientId) + 60;
    }

    /**
     * 获取上下文redis存储Key
     *
     * @param userId 用户ID
     * @return redis Key
     */
    private String getContextKey(Object userId) {
        return "user::context:" + userId.toString();
    }
}
