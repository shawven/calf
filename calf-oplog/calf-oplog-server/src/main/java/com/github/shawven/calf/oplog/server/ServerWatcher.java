package com.github.shawven.calf.oplog.server;

import com.github.shawven.calf.oplog.server.domain.Command;

public interface ServerWatcher {

    void start(Command command);

    void stop(Command command);
}
