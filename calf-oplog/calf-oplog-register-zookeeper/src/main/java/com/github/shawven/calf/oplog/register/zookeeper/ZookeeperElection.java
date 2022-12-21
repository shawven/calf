package com.github.shawven.calf.oplog.register.zookeeper;

import com.github.shawven.calf.oplog.register.election.AbstractElection;
import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.register.election.TaskListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class ZookeeperElection extends AbstractElection {

    private static final Logger logger = LoggerFactory.getLogger(Election.class);

    private final CuratorFramework client;

    private final String path;

    private final String uniqueId;

    private final long ttl;


    public ZookeeperElection(CuratorFramework client, String path, String uniqueId,
                             Long ttl, boolean autoRequeue, TaskListener listener) {
        super(autoRequeue, listener);
        this.client = client;
        this.path = path;
        this.uniqueId = uniqueId != null ? uniqueId : UUID.randomUUID().toString();
        this.ttl = ttl;
    }


    @Override
    protected void startedCallback() throws Exception {

        logger.info("election uses [{}] as uniqueId", uniqueId);
        client.setData().forPath(path, uniqueId.getBytes(StandardCharsets.UTF_8));
    }


    @Override
    protected void lockForStart() throws Exception {
        InterProcessMutex mutex = new InterProcessMutex(client, path);
        // acquire distributed lock

        logger.debug("election get lock");

        mutex.acquire(ttl, TimeUnit.SECONDS);

        logger.debug("election successfully get Lock");
    }
}
