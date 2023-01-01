package com.github.shawven.calf.oplog.server.domain;


import com.github.shawven.calf.oplog.base.EventAction;
import com.github.shawven.calf.oplog.base.EventBaseDTO;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhenhui
 * @Ddate Created in 2018/19/01/2018/3:18 PM
 * @modified by
 */
public class UpdateRowsDTO extends EventBaseDTO implements Serializable {

    private static final long serialVersionUID = 3615869271596131001L;

    private List<UpdateRow> rows;

    public UpdateRowsDTO() {
    }

    public UpdateRowsDTO(EventBaseDTO eventBaseDTO, List<UpdateRow> rows) {
        super(eventBaseDTO.getNamespace(), eventBaseDTO.getEventAction(), eventBaseDTO.getDatabase(), eventBaseDTO.getTable());
        super.setEventAction(EventAction.UPDATE);
        this.rows = rows;
    }

    public List<UpdateRow> getRows() {
        return rows;
    }

    public void setRows(List<UpdateRow> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "UpdateRowsDTO{" +
                "rows=" + rows +
                "} " + super.toString();
    }
}
