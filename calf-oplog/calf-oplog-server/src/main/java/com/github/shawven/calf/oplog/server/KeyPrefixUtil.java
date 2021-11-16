package com.github.shawven.calf.oplog.server;

import com.github.shawven.calf.oplog.base.OplogConstants;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wanglaomo
 * @since 2019/10/16
 **/
public class KeyPrefixUtil {

    private String root;

    public KeyPrefixUtil(String root) {
        this.root = root;
    }

    private AtomicReference<String> prefixCache = new AtomicReference<>();

    public String getPrefix() {

        String prefix = prefixCache.get();
        if(!StringUtils.isEmpty(prefix)) {
            return prefix;
        }

        // init
        if(!root.startsWith(OplogConstants.PATH_SEPARATOR)) {
            root = OplogConstants.PATH_SEPARATOR.concat(root);
        }

        if(!root.endsWith(OplogConstants.PATH_SEPARATOR)) {
            root = root.concat(OplogConstants.PATH_SEPARATOR);
        }

        prefix = root.concat(OplogConstants.DEFAULT_ETCD_METADATA_PREFIX);
        prefixCache.compareAndSet(null, prefix);
        prefix = prefixCache.get();

        return prefix;
    }

    public String withPrefix(String key) {

        if(StringUtils.isEmpty(key)) {
            return getPrefix();
        }

        return getPrefix().concat(key);
    }
}
