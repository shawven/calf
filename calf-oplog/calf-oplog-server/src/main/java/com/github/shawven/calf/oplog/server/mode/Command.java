package com.github.shawven.calf.oplog.server.mode;

/**
 * @author wanglaomo
 * @since 2019/8/7
 **/
public class Command {

    private String namespace;

    private String delegatedIp;

    private CommandType type;

    public Command() {}

    public Command(String namespace, CommandType type) {
        this.namespace = namespace;
        this.type = type;
    }

    public Command(String namespace, String delegatedIp, CommandType type) {
        this.namespace = namespace;
        this.delegatedIp = delegatedIp;
        this.type = type;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public String getDelegatedIp() {
        return delegatedIp;
    }

    public void setDelegatedIp(String delegatedIp) {
        this.delegatedIp = delegatedIp;
    }
}
