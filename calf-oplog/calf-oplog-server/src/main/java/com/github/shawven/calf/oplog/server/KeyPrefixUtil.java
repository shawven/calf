package com.github.shawven.calf.oplog.server;

import com.github.shawven.calf.oplog.base.Consts;
import org.springframework.util.StringUtils;

/**
 * @author wanglaomo
 * @since 2019/10/16
 **/
public class KeyPrefixUtil {

    private final String prefix;

    public KeyPrefixUtil(String root) {
        // init
        if(!root.startsWith("/")) {
            root = "/".concat(root);
        }

        if(!root.endsWith("/")) {
            root = root.concat("/");
        }
        this.prefix = root.concat(Consts.APP_PREFIX);
    }

    public String withPrefix(String key) {
        if (!StringUtils.hasText(key)) {
            return prefix;
        }

        return prefix.concat(key);
    }
}
