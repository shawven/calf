package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.base.DatabaseEvent;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.server.DocumentUtils;
import com.github.shawven.calf.oplog.server.mode.DeleteRowsDTO;
import com.github.shawven.calf.oplog.server.mode.UpdateRow;
import com.github.shawven.calf.oplog.server.mode.UpdateRowsDTO;
import com.github.shawven.calf.oplog.server.mode.WriteRowsDTO;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public interface OpLogEventFormatter {
    /**
     * 格式化参数格式
     *
     * @param event
     * @return 格式化后的string
     */
    EventBaseDTO format(Document event);


    class Write implements OpLogEventFormatter {

        private final String namespace;

        public Write(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public EventBaseDTO format(Document event) {
            WriteRowsDTO writeRowsDTO = new WriteRowsDTO();
            writeRowsDTO.setEventType(DatabaseEvent.WRITE_ROWS);
            //添加表信息
            writeRowsDTO.setDatabase(DocumentUtils.getDataBase(event));
            writeRowsDTO.setTable(DocumentUtils.getTable(event));
            writeRowsDTO.setNamespace(namespace);
            //添加列映射
            Document context = (Document) event.get(OpLogClientFactory.CONTEXT_KEY);
            List<Map<String, Object>> urs = new ArrayList<>();
            urs.add(context);
            writeRowsDTO.setRowMaps(urs);
            return writeRowsDTO;
        }
    }

    class Update implements OpLogEventFormatter {

        private final String namespace;

        public Update(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public EventBaseDTO format(Document event) {
            UpdateRowsDTO updateRowsDTO = new UpdateRowsDTO();
            updateRowsDTO.setEventType(DatabaseEvent.UPDATE_ROWS);
            //添加表信息
            updateRowsDTO.setDatabase(DocumentUtils.getDataBase(event));
            updateRowsDTO.setTable(DocumentUtils.getTable(event));
            updateRowsDTO.setNamespace(namespace);
            //添加列映射

            List<UpdateRow> urs = new ArrayList<>();
            Document updateWhere = (Document)event.get(OpLogClientFactory.UPDATE_WHERE_KEY);
            Document context = (Document) event.get(OpLogClientFactory.CONTEXT_KEY);
            context = (Document) context.get(OpLogClientFactory.UPDATE_CONTEXT_KEY);
            urs.add(new UpdateRow(updateWhere,context));
            updateRowsDTO.setRows(urs);
            return updateRowsDTO;
        }
    }

    class Delete implements OpLogEventFormatter {

        private final String namespace;

        public Delete(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public EventBaseDTO format(Document event) {
            DeleteRowsDTO deleteRowsDTO = new DeleteRowsDTO();
            deleteRowsDTO.setEventType(DatabaseEvent.DELETE_ROWS);
            //添加表信息
            deleteRowsDTO.setDatabase(DocumentUtils.getDataBase(event));
            deleteRowsDTO.setTable(DocumentUtils.getTable(event));
            deleteRowsDTO.setNamespace(namespace);
            //添加列映射
            Document context = (Document) event.get(OpLogClientFactory.CONTEXT_KEY);
            List<Map<String, Object>> urs = new ArrayList<>();
            urs.add(context);
            deleteRowsDTO.setRowMaps(urs);
            return deleteRowsDTO;
        }
    }
}
