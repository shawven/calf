package com.test.mapper.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-03-15 11:13
 */
public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {

    default int update(T entity, T condition) {
        return update(entity, new UpdateWrapper<>(condition));
    }

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

    default IPage selectPage(IPage<T> page, T entity) {
        return selectPage(page, new QueryWrapper<>(entity));
    }

    default IPage<Map<String, Object>> selectMapsPage(IPage<T> page, T entity) {
        return selectMapsPage(page, new QueryWrapper<>(entity));
    }
}
