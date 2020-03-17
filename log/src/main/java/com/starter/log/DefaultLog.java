package com.starter.log;

import com.starter.log.core.Recordable;
import com.starter.log.emun.LogType;

/**
 * @author Shoven
 * @date 2019-07-26 16:27
 */
public class DefaultLog implements Recordable {

    private String module;

    private String typeName;

    private String desc;

    private Long cost;

    private String error;

    @Override
    public void setModule(String module) {
        this.module = module;
    }

    public String getModule() {
        return module;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public void setLogType(LogType type) {
        setTypeName(type.getTypeName());
    }

    @Override
    public String toString() {
        return "DefaultRecord{" +
                "module='" + module + '\'' +
                ", typeName='" + typeName + '\'' +
                ", desc='" + desc + '\'' +
                ", usage=" + cost +
                ", error='" + error + '\'' +
                '}';
    }
}
