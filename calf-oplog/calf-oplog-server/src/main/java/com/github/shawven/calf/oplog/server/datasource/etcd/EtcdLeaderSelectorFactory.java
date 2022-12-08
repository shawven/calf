package com.github.shawven.calf.oplog.server.datasource.etcd;

import com.github.shawven.calf.oplog.server.datasource.LeaderSelectorFactory;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.LeaderSelector;
import com.github.shawven.calf.oplog.server.core.OplogTaskListener;
import io.etcd.jetcd.Client;

/**
 * @author xw
 * @date 2022/11/30
 */
public class EtcdLeaderSelectorFactory implements LeaderSelectorFactory {

    private Client client;

    public EtcdLeaderSelectorFactory(Client client) {
        this.client = client;
    }

    @Override
    public LeaderSelector getLeaderSelector(String namespace, long timeToLive, String identification,
                                            String identificationPath, OplogTaskListener listener) {

        return new EtcdLeaderSelector(client, namespace, timeToLive, identification, identificationPath, listener);
    }
}
