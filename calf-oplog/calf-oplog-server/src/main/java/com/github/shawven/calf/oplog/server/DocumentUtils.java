package com.github.shawven.calf.oplog.server;

import com.github.shawven.calf.oplog.server.core.OpLogClientFactory;
import org.bson.Document;

/**
 * @author xw
 * @date 2022/12/8
 */
public class DocumentUtils {

    public static String getDataBase(Document event){
        String dataBaseTable = event.getString(OpLogClientFactory.DATABASE_KEY);
        return dataBaseTable.split("\\.")[0];
    }

    public static String getTable(Document event){
        String dataBaseTable = event.getString(OpLogClientFactory.DATABASE_KEY);
        return dataBaseTable.split("\\.")[1];
    }
}
