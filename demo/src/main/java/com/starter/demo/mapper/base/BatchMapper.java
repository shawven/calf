package com.starter.demo.mapper.base;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.session.SqlSession;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author Shoven
 * @date 2019-11-25
 */
public interface BatchMapper<T> {

    default boolean saveBatch(Collection<T> entityList) {
        return saveBatch(entityList, 1000);
    }

    @Transactional(rollbackFor = Exception.class)
    default boolean saveBatch(Collection<T> entityList, int batchSize) {
        String sqlStatement = SqlHelper.table(currentModelClass()).getSqlStatement(SqlMethod.INSERT_ONE.getMethod());
        try (SqlSession batchSqlSession = SqlHelper.sqlSessionBatch(currentModelClass())) {
            int i = 0;
            for (T anEntityList : entityList) {
                batchSqlSession.insert(sqlStatement, anEntityList);
                if (i >= 1 && i % batchSize == 0) {
                    batchSqlSession.flushStatements();
                }
                i++;
            }
            batchSqlSession.flushStatements();
        }
        return true;
    }

    default boolean updateBatchById(Collection<T> entityList) {
        return updateBatchById(entityList, 1000);
    }

    @Transactional(rollbackFor = Exception.class)
    default boolean updateBatchById(Collection<T> entityList, int batchSize) {
        Assert.notEmpty(entityList, "error: entityList must not be empty");
        String sqlStatement = SqlHelper.table(currentModelClass()).getSqlStatement(SqlMethod.UPDATE_BY_ID.getMethod());
        try (SqlSession batchSqlSession = SqlHelper.sqlSessionBatch(currentModelClass())) {
            int i = 0;
            for (T anEntityList : entityList) {
                MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
                param.put(Constants.ENTITY, anEntityList);
                batchSqlSession.update(sqlStatement, param);
                if (i >= 1 && i % batchSize == 0) {
                    batchSqlSession.flushStatements();
                }
                i++;
            }
            batchSqlSession.flushStatements();
        }
        return true;
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    default Class<T> currentModelClass() {
        Class<? extends BatchMapper> cls = getClass();
        String simpleName = cls.getSimpleName();
        // mapper调用实际是代理类
        Type[] interfaces = cls.getGenericInterfaces();
        // 这里还是具体的mapper
        Class currentInterfaceCls = (Class)interfaces[0];
        // 通用mapper
        Type[] genericInterfaces = currentInterfaceCls.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genericInterface).getActualTypeArguments();
                if (params.length >= 1) {
                    return (Class<T>)params[0];
                }
            }
        }
        throw new IllegalArgumentException(String.format("Class %s not ParameterizedType from interfaces ", simpleName));
    }
}
