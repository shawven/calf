package com.github.shawven.calf.oplog.register.etcd;

import com.github.shawven.calf.oplog.register.ElectionFactory;
import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.register.election.TaskListener;
import io.etcd.jetcd.Client;

/**
 * @author xw
 * @date 2022/11/30
 */
public class EtcdElectionFactory implements ElectionFactory {

    private static final String DEFAULT_PATH = "/leader-selector/";

    private final Client client;

    public EtcdElectionFactory(Client client) {
        this.client = client;
    }

    @Override
    public Election getElection(String path, String namespace, String uniqueId,
                                long ttl, TaskListener listener) {

        path = path != null
                ? path.endsWith("/") ? path : path.concat("/")
                : DEFAULT_PATH;

        return new EtcdElection(client, path + namespace, uniqueId, ttl, true, listener);
    }
}
