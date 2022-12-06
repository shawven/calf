package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.base.DatabaseEvent;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.mode.DeleteRowsDTO;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogDeleteEventHandler extends AbstractOpLogEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OpLogDeleteEventHandler.class);

    public OpLogDeleteEventHandler(String namespace, DataPublisherManager dataPublisherManager, Map<String, Set<ClientInfo>> clientInfoMap) {
        super(namespace, dataPublisherManager, clientInfoMap);
    }

    @Override
    protected EventBaseDTO formatData(Document event) {
        DeleteRowsDTO deleteRowsDTO = new DeleteRowsDTO();
        deleteRowsDTO.setEventType(DatabaseEvent.DELETE_ROWS);
        //添加表信息
        deleteRowsDTO.setDatabase(super.getDataBase(event));
        deleteRowsDTO.setTable(super.getTable(event));
        deleteRowsDTO.setNamespace(getNamespace());
        //添加列映射
        Document context = (Document) event.get(OpLogClientFactory.CONTEXT_KEY);
        List<Map<String, Object>> urs = new ArrayList<>();
        urs.add(context);
        deleteRowsDTO.setRowMaps(urs);
        return deleteRowsDTO;
    }
}
