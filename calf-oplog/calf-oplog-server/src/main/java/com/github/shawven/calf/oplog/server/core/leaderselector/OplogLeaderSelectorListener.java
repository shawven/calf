package com.github.shawven.calf.oplog.server.core.leaderselector;

import com.github.shawven.calf.extension.BinaryLogConfig;
import com.github.shawven.calf.extension.ConfigDataSource;
import com.github.shawven.calf.oplog.server.core.OpLogClientFactory;
import com.github.shawven.calf.oplog.server.core.OpLogEventHandler;
import com.github.shawven.calf.oplog.server.core.OplogClient;
import io.reactivex.disposables.Disposable;
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
    private BinaryLogConfig binaryLogConfig;
    private ConfigDataSource configDataSource;
    private Disposable disposable;

    public OplogLeaderSelectorListener(OpLogClientFactory opLogClientFactory,
                                       BinaryLogConfig binaryLogConfig,
                                       ConfigDataSource configDataSource) {
        this.opLogClientFactory = opLogClientFactory;
        this.binaryLogConfig = binaryLogConfig;
        this.configDataSource = configDataSource;
    }

    @Override
    public void afterTakeLeadership() {

        this.oplogClient = opLogClientFactory.initClient(binaryLogConfig);
        // 启动连接
        try {
            disposable = oplogClient.getOplog().subscribe(document -> {
                opLogClientFactory.eventCount.incrementAndGet();
                String eventType = document.getString(EVENTTYPE_KEY);
                OpLogEventHandler handler = oplogClient.getOpLogEventHandlerFactory().getHandler(eventType);
                handler.handle(document);
            });
            binaryLogConfig.setActive(true);
            binaryLogConfig.setVersion(binaryLogConfig.getVersion() + 1);
            configDataSource.update(binaryLogConfig);
        } catch (Exception e) {
            // TODO: 17/01/2018 继续优化异常处理逻辑
            logger.error("[" + binaryLogConfig.getNamespace() + "] 处理事件异常，{}", e);
        }

    }

    @Override
    public boolean afterLosingLeadership() {
        disposable.dispose();
        binaryLogConfig.setActive(false);
        configDataSource.update(binaryLogConfig);
        return opLogClientFactory.closeClient(oplogClient, binaryLogConfig.getNamespace());
    }
}
