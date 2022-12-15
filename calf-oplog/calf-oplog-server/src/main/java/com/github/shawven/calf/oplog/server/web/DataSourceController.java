package com.github.shawven.calf.oplog.server.web;

import com.alibaba.fastjson.JSONObject;
import com.github.shawven.calf.oplog.register.domain.InstanceStatus;
import com.github.shawven.calf.oplog.server.core.ReplicationServer;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/10
 **/
@RestController
@RequestMapping("/datasource")
public class DataSourceController {

    private final ReplicationServer replicationServer;

    public DataSourceController(ReplicationServer replicationServer) {
        this.replicationServer = replicationServer;
    }

    @GetMapping("/list")
    public List<DataSourceCfg> datasourceConfigs() {
        return replicationServer.getAllConfigs();
    }

    @PostMapping("/persist")
    public Result persistDatasourceConfig(@RequestBody DataSourceCfg config) {

        if(replicationServer.persistDatasourceConfig(config)) {
            return new Result(Result.SUCCESS, "添加数据源成功");
        } else {
            return new Result(Result.ERROR, "添加数据源失败，命名空间已存在");
        }
    }

    @PostMapping("/remove")
    public Result removeDatasourceConfig(@RequestBody JSONObject JsonObject) {

        if(replicationServer.removeDatasourceConfig(JsonObject.getString("namespace"))) {
            return new Result(Result.SUCCESS, "移除数据源成功");
        } else {
            return new Result(Result.ERROR, "移除数据源失败");
        }
    }

    @PostMapping("/start")
    public Result startDatasource(@RequestBody JSONObject jsonObject) {

        String namespace = jsonObject.getString("namespace");
        String delegatedIp = jsonObject.getString("delegatedIp");

        if(replicationServer.startDatasource(namespace, delegatedIp)) {
            return new Result(Result.SUCCESS, "发送开启数据源监听命令成功");
        } else {
            return new Result(Result.ERROR, "发送开启数据源监听命令失败");
        }
    }

    @PostMapping("/stop")
    public Result stopDatasource(@RequestBody JSONObject JsonObject) {

        if(replicationServer.stopDatasource(JsonObject.getString("namespace"))) {
            return new Result(Result.SUCCESS, "发送关闭数据源监听命令成功");
        } else {
            return new Result(Result.ERROR, "发送关闭数据源监听命令失败");
        }
    }

    @GetMapping("/service-status")
    public List<InstanceStatus> getServiceStatus() {
        return replicationServer.getServiceStatus();
    }
}
