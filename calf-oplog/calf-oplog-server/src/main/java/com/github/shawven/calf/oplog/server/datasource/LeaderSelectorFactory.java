package com.github.shawven.calf.oplog.server.datasource;

import com.github.shawven.calf.oplog.server.datasource.leaderselector.LeaderSelector;
import com.github.shawven.calf.oplog.server.core.OplogTaskListener;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.TaskListener;

/**
 * @author xw
 * @date 2022/11/30
 */
public interface LeaderSelectorFactory {

    LeaderSelector getLeaderSelector(String path, String namespace, String uniqueId, long ttl, TaskListener listener);
}
