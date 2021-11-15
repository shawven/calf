package com.github.shawven.calf.oplog.server.core;


import oplog.client.model.ClientInfo;
import oplog.client.model.dto.EventBaseDTO;
import oplog.client.model.dto.UpdateRow;
import oplog.client.model.dto.UpdateRowsDTO;
import com.github.shawven.calf.base.DatabaseEvent;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogUpdateEventHandler extends OpLogEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OpLogUpdateEventHandler.class);

    public OpLogUpdateEventHandler(OpLogEventContext context) {
        super(context);
    }

    @Override
    protected EventBaseDTO formatData(Document event) {
        UpdateRowsDTO updateRowsDTO = new UpdateRowsDTO();
        updateRowsDTO.setEventType(DatabaseEvent.UPDATE_ROWS);
        //添加表信息
        updateRowsDTO.setDatabase(super.getDataBase(event));
        updateRowsDTO.setTable(super.getTable(event));
        updateRowsDTO.setNamespace(context.getBinaryLogConfig().getNamespace());
        //添加列映射

        List<UpdateRow> urs = new ArrayList<>();
        Document updateWhere = (Document)event.get(OpLogClientFactory.UPDATE_WHERE_KEY);
        Document context = (Document) event.get(OpLogClientFactory.CONTEXT_KEY);
        context = (Document) context.get(OpLogClientFactory.UPDATE_CONTEXT_KEY);
        urs.add(new UpdateRow(updateWhere,context));
        updateRowsDTO.setRows(urs);
        return updateRowsDTO;
    }

    @Override
    protected Set<ClientInfo> filter(Document event) {
        String database = super.getDataBase(event);
        String table = super.getTable(event);
        String tableKey = database.concat("/").concat(table);
        return clientInfoMap.get(tableKey);
    }
}
