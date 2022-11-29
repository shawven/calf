package com.example.nativepractice.constant;

/**
 * 缓存名
 *
 * @author Shoven
 * @date 2020-10-29
 */
public final class CacheKey {

    /**
     * 应用前缀
     */
    private static final String APPLICATION_PREFIX = "lightcloud:";

    /**
     * 应用权限缓存前缀
     */
    private static final String AUTH_APP_VISIBLE_PREFIX = APPLICATION_PREFIX + "auth:app_visible:";

    /**
     * 权限中心缓存前缀
     */
    private static final String AUTH_CENTER_SUPER_BIZ_ADMIN_PREFIX = APPLICATION_PREFIX + "auth:center:biz";
    private static final String AUTH_CENTER_SUPER_GRANT_ADMIN_PREFIX = APPLICATION_PREFIX + "auth:center:grant";

    /**
     * 固定名称
     */
    public static class Const {
        /**
         * 卡片缓存
         */
        public static final String AUTH_APP_CLEANER = APPLICATION_PREFIX + "auth:app_cleaner";
    }

    /**
     * @param key
     * @return
     */
    public static String authAppVisible(String key) {
        return AUTH_APP_VISIBLE_PREFIX + key;
    }

    public static String authCenterSuperBizAdmin(String key) {
        return AUTH_CENTER_SUPER_BIZ_ADMIN_PREFIX + key;
    }

    public static String authCenterSuperGrantAdmin(String key) {
        return AUTH_CENTER_SUPER_GRANT_ADMIN_PREFIX + key;
    }
}
