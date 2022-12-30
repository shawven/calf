package com.github.shawven.calf.oplog.base;

/**
 * @author wanglaomo
 * @since 2019/6/4
 **/
public interface Const {

    String DEFAULT_NAMESPACE = "default";

    String APP_PREFIX = "oplog";

    String COMMAND = "command";

    String NODE_CONFIG = "node-config";

    String LEADER_PATH = "leader-selector";

    String SERVICE_STATUS_PATH =  "service-status";

    String REDIS_PREFIX = "oplog::";

    String TOPIC_EVENT_DATA = APP_PREFIX + "_" + "event_data";

    String QUEUE_TYPE_REDIS = "redis";
    String QUEUE_TYPE_RABBIT = "rabbit";
    String QUEUE_TYPE_KAFKA = "kafka";
}
