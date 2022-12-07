package com.github.shawven.calf.oplog.server.datasource.leaderselector;

import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.core.OpLogClientFactory;
import com.github.shawven.calf.oplog.server.core.AbstractOpLogEventHandler;
import com.github.shawven.calf.oplog.server.core.OplogClient;
import io.reactivex.rxjava3.disposables.Disposable;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.shawven.calf.oplog.server.core.OpLogClientFactory.EVENTTYPE_KEY;


/**
 * @author: kl @kailing.pub
 * @date: 2019/8/1
 */
public class OplogLeaderSelectorListener implements LeaderSelectorListener {

    Logger logger = LoggerFactory.getLogger(getClass());

    private OpLogClientFactory opLogClientFactory;
    private OplogClient oplogClient;
    private NodeConfig nodeConfig;
    private ClientDataSource clientDataSource;
    private NodeConfigDataSource nodeConfigDataSource;
    private Disposable disposable;

    public OplogLeaderSelectorListener(OpLogClientFactory opLogClientFactory,
                                       NodeConfig nodeConfig,
                                       ClientDataSource clientDataSource,
                                       NodeConfigDataSource nodeConfigDataSource) {
        this.opLogClientFactory = opLogClientFactory;
        this.nodeConfig = nodeConfig;
        this.clientDataSource = clientDataSource;
        this.nodeConfigDataSource = nodeConfigDataSource;
    }

    @Override
    public void afterTakeLeadership() {

        this.oplogClient = opLogClientFactory.initClient(nodeConfig);
        // 启动连接
        try {
            disposable = oplogClient.getOplog().subscribe(document -> {
                opLogClientFactory.eventCount.incrementAndGet();
                String eventType = document.getString(EVENTTYPE_KEY);
                AbstractOpLogEventHandler handler = oplogClient.getOpLogEventHandlerFactory().getHandler(eventType);
                handler.handle(document);
                updateOpLogStatus(document);
            });
            nodeConfig.setActive(true);
            nodeConfig.setVersion(nodeConfig.getVersion() + 1);
            nodeConfigDataSource.update(nodeConfig);
        } catch (Exception e) {
            // TODO: 17/01/2018 继续优化异常处理逻辑
            logger.error("[" + nodeConfig.getNamespace() + "] 处理事件异常，{}", e);
        }

    }

    @Override
    public boolean afterLosingLeadership() {
        disposable.dispose();
        nodeConfig.setActive(false);
        nodeConfigDataSource.update(nodeConfig);
        opLogClientFactory.closeClient(oplogClient, nodeConfig.getNamespace());
        return true;
    }

    /**
     * 更新日志位置
     *
     * @param document
     */
    protected void updateOpLogStatus(Document document) {
        BsonTimestamp ts = (BsonTimestamp) document.get(OpLogClientFactory.TIMESTAMP_KEY);
        clientDataSource.updateBinLogStatus(String.valueOf(ts.getTime()), ts.getInc(), nodeConfig, System.currentTimeMillis());
    }
}
