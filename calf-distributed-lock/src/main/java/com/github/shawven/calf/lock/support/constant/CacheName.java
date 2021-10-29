package com.github.shawven.calf.lock.support.constant;


/**
 * 缓存名
 *
 * @author Shoven
 * @date 2020-10-29
 */
public final class CacheName {

    /**
     * 应用前缀
     */
    public static final String APPLICATION_PREFIX = "demo:";

    /**
     * cookie前缀
     */
    private static final String COOKIE_PREFIX = APPLICATION_PREFIX + "cookie";

    /**
     * 设置前缀
     */
    private static final String SETTING_PREFIX = APPLICATION_PREFIX + "setting";

    /**
     * 全局锁前缀
     */
    private static final String GLOBAL_LOCK_PREFIX = APPLICATION_PREFIX + "glock";

    /**
     * 选项前缀
     */
    private static final String OPTION_PREFIX = APPLICATION_PREFIX + "option";

    /**
     * cookie
     *
     * @param key
     * @return
     */
    public static String cookie(String key) {
        return COOKIE_PREFIX + ":" + key;
    }


    /**
     * 全局锁
     *
     * @param name
     * @return
     */
    public static String globalLock(String name) {
        return GLOBAL_LOCK_PREFIX + ":" + name;
    }

    /**
     * 设置
     *
     * @param key
     * @return
     */
    public static String setting(String key) {
        return SETTING_PREFIX + ":" + key;
    }

    /**
     * 设置
     *
     * @param key
     * @return
     */
    public static String option(String key) {
        return OPTION_PREFIX + ":" + key;
    }
}
