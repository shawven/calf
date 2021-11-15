package com.github.shawven.calf.oplog.server.core;

import oplog.client.model.ClientInfo;
import oplog.client.model.dto.EventBaseDTO;
import oplog.client.model.dto.WriteRowsDTO;
import com.github.shawven.calf.base.DatabaseEvent;
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
public class OpLogWriteEventHandler extends OpLogEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OpLogWriteEventHandler.class);

    public OpLogWriteEventHandler(OpLogEventContext context) {
        super(context);
    }

    @Override
    protected EventBaseDTO formatData(Document event) {
        WriteRowsDTO writeRowsDTO = new WriteRowsDTO();
        writeRowsDTO.setEventType(DatabaseEvent.WRITE_ROWS);
        //添加表信息
        writeRowsDTO.setDatabase(super.getDataBase(event));
        writeRowsDTO.setTable(super.getTable(event));
        writeRowsDTO.setNamespace(context.getBinaryLogConfig().getNamespace());
        //添加列映射
        Document context = (Document) event.get(OpLogClientFactory.CONTEXT_KEY);
        List<Map<String, Object>> urs = new ArrayList<>();
        urs.add(context);
        writeRowsDTO.setRowMaps(urs);
        return writeRowsDTO;
    }

    @Override
    protected Set<ClientInfo> filter(Document event) {
        String database = super.getDataBase(event);
        String table = super.getTable(event);
        String tableKey = database.concat("/").concat(table);
        return clientInfoMap.get(tableKey);
    }
}
