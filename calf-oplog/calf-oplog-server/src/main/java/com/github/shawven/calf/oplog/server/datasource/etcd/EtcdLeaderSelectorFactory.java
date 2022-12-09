package com.github.shawven.calf.oplog.server.datasource.etcd;

import com.github.shawven.calf.oplog.server.datasource.LeaderSelectorFactory;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.LeaderSelector;
import com.github.shawven.calf.oplog.server.core.OplogTaskListener;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.TaskListener;
import io.etcd.jetcd.Client;
import org.springframework.util.StringUtils;

/**
 * @author xw
 * @date 2022/11/30
 */
public class EtcdLeaderSelectorFactory implements LeaderSelectorFactory {

    private static final String DEFAULT_PATH = "/leader-selector/";

    private final Client client;

    public EtcdLeaderSelectorFactory(Client client) {
        this.client = client;
    }

    @Override
    public LeaderSelector getLeaderSelector(String path, String namespace, String uniqueId,
                                            long ttl, TaskListener listener) {

        path = StringUtils.hasText(path)
                ? path.endsWith("/") ? path : path.concat("/")
                : DEFAULT_PATH;
        
        return new EtcdLeaderSelector(client, path + namespace, uniqueId, ttl, true, listener);
    }
}
