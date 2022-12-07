package com.github.shawven.calf.oplog.server.publisher;

import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.base.LockLevel;
import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.mode.DeleteRowsDTO;
import com.github.shawven.calf.oplog.server.mode.UpdateRow;
import com.github.shawven.calf.oplog.server.mode.UpdateRowsDTO;
import com.github.shawven.calf.oplog.server.mode.WriteRowsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/4:26 PM
 * @modified by
 */
public class DataPublisherManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPublisherManager.class);

    public static final String DATA = "BIN-LOG-DATA-";

    private Map<String, DataPublisher> dataPublisherMap;


    private AtomicLong publishCount = new AtomicLong(0);

    private long lastPublishCount = 0;

    public DataPublisherManager(Map<String, DataPublisher> dataPublisherMap) {
        this.dataPublisherMap = dataPublisherMap;
    }

    public void publish(Set<ClientInfo> clientInfos, EventBaseDTO data) {
        clientInfos.forEach(clientInfo -> {
            if (LockLevel.COLUMN.equals(clientInfo.getLockLevel())) {
                List<Map<String, Object>> rowMaps;
                //如果锁是列级别的特殊处理
                switch (clientInfo.getDatabaseEvent()) {
                    case UPDATE_ROWS:
                        //更新行要比对前后
                        UpdateRowsDTO udto = (UpdateRowsDTO) data;
                        List<UpdateRow> rows = udto.getRows();
                        rows.forEach(updateRow -> {
                            Map<String, Object> bm = updateRow.getBeforeRowMap();
                            Map<String, Object> am = updateRow.getAfterRowMap();
                            Object bCloumn = bm.get(clientInfo.getColumnName()) == null ? "NULL" : bm.get(clientInfo.getColumnName());
                            Object aCloumn = am.get(clientInfo.getColumnName()) == null ? "NULL" : am.get(clientInfo.getColumnName());
                            if (bCloumn.equals(aCloumn)) {
                                //如果两个一致，即变更的不是作为key的列
                                doPublish(clientInfo, DATA.concat(clientInfo.getKey()).concat(bCloumn.toString()), new UpdateRowsDTO(data, Arrays.asList(updateRow)));
                            } else {
                                //对老的来说是删除
                                doPublish(clientInfo, DATA.concat(clientInfo.getKey()).concat(bCloumn.toString()), new DeleteRowsDTO(data, Arrays.asList(bm)));
                                //对新的来说是插入
                                doPublish(clientInfo, DATA.concat(clientInfo.getKey()).concat(aCloumn.toString()), new WriteRowsDTO(data, Arrays.asList(am)));
                            }
                        });
                        break;
                    case WRITE_ROWS:
                        WriteRowsDTO wdto = (WriteRowsDTO) data;
                        rowMaps = wdto.getRowMaps();
                        rowMaps.forEach(r -> {
                            Object cn = r.get(clientInfo.getColumnName()) == null ? "NULL" : r.get(clientInfo.getColumnName());
                            doPublish(clientInfo, DATA.concat(clientInfo.getKey()).concat(cn.toString()), new WriteRowsDTO(data, Arrays.asList(r)));
                        });
                        break;
                    case DELETE_ROWS:
                        DeleteRowsDTO ddto = (DeleteRowsDTO) data;
                        rowMaps = ddto.getRowMaps();
                        rowMaps.forEach(r -> {
                            Object cn = r.get(clientInfo.getColumnName()) == null ? "NULL" : r.get(clientInfo.getColumnName());
                            doPublish(clientInfo, DATA.concat(clientInfo.getKey()).concat(cn.toString()), new DeleteRowsDTO(data, Arrays.asList(r)));
                        });
                        break;
                        default:
                            LOGGER.info("不支持的事件类型");
                }
            } else {
                //其他级别直接发布
                doPublish(clientInfo, topicName(clientInfo), data);
            }

            publishCount.incrementAndGet();
        });
    }

    private void doPublish(ClientInfo clientInfo, String dataKey, EventBaseDTO data) {
        selectDataPublisher(clientInfo).publish(clientInfo.getClientId(), dataKey, data);
    }

    /**
     * 默认表级锁定topicName
     * @param clientInfo
     * @return
     */
    public static String topicName(ClientInfo clientInfo){
        return DATA.concat(clientInfo.getKey());
    }


    public long getPublishCount() {
        return publishCount.get();
    }

    public long publishCountSinceLastTime() {
        long total = publishCount.get();
        long res =  total - lastPublishCount;

        lastPublishCount = total;

        return res;
    }

    public DataPublisher selectDataPublisher(ClientInfo clientInfo) {
        return dataPublisherMap.get(clientInfo.getQueueType().toLowerCase() + "DataPublisher");
    }
}
