package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.register.ElectionFactory;
import com.github.shawven.calf.oplog.server.ops.ClientOps;
import com.github.shawven.calf.oplog.server.ops.DataSourceCfgOps;
import com.github.shawven.calf.oplog.server.ops.StatusOps;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.register.domain.InstanceStatus;
import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.github.shawven.calf.oplog.server.support.KeyUtils;
import com.github.shawven.calf.oplog.server.support.NetUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class MongoReplicationServerImpl extends AbstractReplicationServer {

    public static final String TYPE = "MongoDB";

    private final OpLogClientFactory opLogClientFactory;

    private final DataSourceCfgOps dataSourceCfgOps;

    private final DataPublisherManager dataPublisherManager;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private final ElectionFactory electionFactory;

    private final Map<String, Election> electionMap = new ConcurrentHashMap<>();

    public MongoReplicationServerImpl(OpLogClientFactory opLogClientFactory, ElectionFactory electionFactory,
                                      DataSourceCfgOps dataSourceCfgOps, ClientOps clientOps, StatusOps statusOps,
                                     DataPublisherManager dataPublisherManager) {
        super(dataSourceCfgOps, clientOps, statusOps);
        this.opLogClientFactory = opLogClientFactory;
        this.electionFactory = electionFactory;
        this.dataSourceCfgOps = dataSourceCfgOps;
        this.dataPublisherManager = dataPublisherManager;
    }

    @Override
    public String dataSourceType() {
        return TYPE;
    }

    @Override
    public void doStart(DataSourceCfg dataSourceCfg) {
        executorService.submit(() -> {
            String path = KeyUtils.withPrefix(Const.LEADER_PATH);
            String namespace = dataSourceCfg.getNamespace();
            String uniqueId = NetUtils.getLocalAddress().getHostAddress();

            OplogElectionListener listener = new OplogElectionListener(dataSourceCfg,
                    opLogClientFactory, statusOps, clientOps, dataSourceCfgOps, dataPublisherManager);

            Election election = electionFactory.getElection(path, namespace, uniqueId, 20L, listener);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("shutdownHook trigger close");
                election.close();
            }));

            electionMap.put(namespace, election);
            election.start();
        });
    }

    @Override
    public void stop(String namespace) {
        Election election = electionMap.get(namespace);
        if(election != null) {
            election.close();
        }
    }

    @Override
    protected void updateInstanceStatus() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            InstanceStatus instanceStatus = new InstanceStatus();
            String localIp = TYPE + ":" + NetUtils.getLocalAddress().getHostAddress();
            instanceStatus.setIp(localIp);
            instanceStatus.setTotalEventCount(opLogClientFactory.getEventCount());
            instanceStatus.setLatelyEventCount(opLogClientFactory.eventCountSinceLastTime());
            instanceStatus.setTotalPublishCount(dataPublisherManager.getPublishCount());
            instanceStatus.setLatelyPublishCount(dataPublisherManager.publishCountSinceLastTime());
            instanceStatus.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            statusOps.updateInstanceStatus(localIp, instanceStatus);
            logger.info("updateInstanceStatus: [{}]", instanceStatus);
        }, 10, 20, TimeUnit.SECONDS);
    }
}
