package com.github.shawven.calf.examples.oauth2.mapper.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-03-15 11:13
 */
public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T>, BatchMapper<T> {

    default T selectOne(T entity) {
        return selectOne(new QueryWrapper<>(entity));
    }

    default Integer selectCount(T entity) {
        return selectCount(new QueryWrapper<>(entity));
    }

    default List<T> selectList(T entity) {
        return selectList(new QueryWrapper<>(entity));
    }

    default  List<Map<String, Object>> selectMaps(T entity) {
        return selectMaps(new QueryWrapper<>(entity));
    }

    default List<Object> selectObjs(T entity) {
        return selectObjs(new QueryWrapper<>(entity));
    }

    default IPage<T> selectPage(IPage<T> page, T entity) {
        return selectPage(page, new QueryWrapper<>(entity));
    }

    default IPage<Map<String, Object>> selectMapsPage(IPage<Map<String, Object>> page, T entity) {
        return selectMapsPage(page,new QueryWrapper<>(entity));
    }

    default int update(T entity, T condition) {
        return update(entity, new UpdateWrapper<>(condition));
    }

    default int delete(T condition) {
        return delete(new QueryWrapper<>(condition));
    }
}
