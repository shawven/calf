package com.github.shawven.calf.oplog.register.election;

public interface LeaderLatchListener {

    void isLeader();

    void notLeader();
}
