package com.test.app.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Shoven
 * @date 2018-11-07 17:49
 */
public interface BaseService<T> extends IService<T> {

    boolean remove(T entity);

    boolean update(T entity, T condition);

    @Override
    List<T> listByIds(Collection<? extends Serializable> idList);

    @Override
    List<T> listByMap(Map<String, Object> columnMap);

    T getOne(T entity);

    T getOne(T entity, boolean throwable);

    Map<String, Object> getMap(T entity);

    Object getObj(T entity);

    int count(T entity);

    List<T> list(T entity);

    IPage<T> page(IPage<T> page, T entity);

    List<Map<String, Object>> listMaps(T entity);

    List<Object> listObjs(T entity);

    <R> List<R> listObjs(T entity, Function<? super Object, R> mapper);

    IPage<Map<String, Object>> pageMaps(IPage<T> page, T entity);
}
