package com.github.shawven.calf.oplog.server.support;

import com.github.shawven.calf.oplog.base.Const;
import org.springframework.util.StringUtils;

/**
 * @author wanglaomo
 * @since 2019/10/16
 **/
public class KeyUtils {

    private static String prefix;

    public static void setRoot(String root) {
        // init
        root = ensureStartSlash(root);
        root = ensureEndSlash(root);

        KeyUtils.prefix = root.concat(ensureEndSlash(Const.DATA_SOURCE));
    }

    private KeyUtils() {}

    public static String withPrefix(String key) {
        if (!StringUtils.hasText(key)) {
            return prefix;
        }

        return prefix.concat(removeStartSlash(key));
    }

    public static String removeStartSlash(String path) {
        return path.startsWith("/") ? path.substring(1, path.length() - 1) : path;
    }

    public static String ensureStartSlash(String path) {
        return !path.startsWith("/") ? "/".concat(path) : path;
    }

    public static String ensureEndSlash(String path) {
        return !path.endsWith("/") ? path.concat("/") : path;
    }
}
