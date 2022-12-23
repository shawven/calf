package com.github.shawven.calf.oplog.register;

import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.register.election.ElectionListener;

/**
 * @author xw
 * @date 2022/11/30
 */
public interface ElectionFactory {

    Election getElection(String path, String namespace, String uniqueId, long ttl, ElectionListener listener);
}
