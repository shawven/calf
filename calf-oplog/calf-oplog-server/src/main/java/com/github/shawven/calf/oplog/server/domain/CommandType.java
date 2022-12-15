package com.github.shawven.calf.oplog.server.domain;

public enum CommandType {

    START_DATASOURCE("START_DATASOURCE", "开启数据源"),
    STOP_DATASOURCE("STOP_DATASOURCE", "关闭数据源");

    private final String code;

    private final String description;

    CommandType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
