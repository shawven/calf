package com.github.shawven.calf.oplog.server.datasource;


import io.reactivex.rxjava3.core.Emitter;

import java.util.List;

public interface DataSource {

    List<String> list(String key);

    String get(String key);

    void set(String key, String val);

    void set(String key, String val, long ttl);

    void watch(String key, Emitter<String> emitter);
}
