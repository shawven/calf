package com.github.shawven.calf.oplog.server;

import com.github.shawven.calf.oplog.base.Const;
import org.springframework.util.StringUtils;

/**
 * @author wanglaomo
 * @since 2019/10/16
 **/
public class KeyPrefixUtil {

    private final String prefix;

    public KeyPrefixUtil(String root) {
        // init
        root = ensureStartSlash(root);
        root = ensureEndSlash(root);

        this.prefix = root.concat(ensureEndSlash(Const.APP_PREFIX));
    }

    public String withPrefix(String key) {
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
