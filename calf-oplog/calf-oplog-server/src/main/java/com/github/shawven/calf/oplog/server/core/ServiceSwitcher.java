package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.server.mode.Command;

public interface ServiceSwitcher {

    void start(Command command);

    void stop(Command command);
}
