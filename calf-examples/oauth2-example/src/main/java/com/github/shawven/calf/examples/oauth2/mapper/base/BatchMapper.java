package com.github.shawven.calf.examples.oauth2.mapper.base;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.shawven.calf.examples.oauth2.service.AopSupport;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Shoven
 * @date 2019-11-25
 */
public interface BatchMapper<T> extends AopSupport {

    Log log = LogFactory.getLog(BatchMapper.class);

    /**
     * 批量新增
     *
     * @param entityList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveBatch(Collection<T> entityList) {
        return saveBatch(entityList, 1000);
    }

    /**
     * 批量新增
     *
     * @param entityList
     * @param batchSize
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveBatch(Collection<T> entityList, int batchSize) {
        String sqlStatement = SqlHelper.table(currentModelClass()).getSqlStatement(SqlMethod.INSERT_ONE.getMethod());
        return executeBatch(entityList, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
    }

    /**
     * 批量更新
     *
     * @param entityList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateBatchById(Collection<T> entityList) {
        return updateBatchById(entityList, 1000);
    }

    /**
     * 批量更新
     *
     * @param entityList
     * @param batchSize
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateBatchById(Collection<T> entityList, int batchSize) {
        String sqlStatement = SqlHelper.table(currentModelClass()).getSqlStatement(SqlMethod.UPDATE_BY_ID.getMethod());
        return executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
            param.put(Constants.ENTITY, entity);
            sqlSession.update(sqlStatement, param);
        });
    }

    /**
     * 批量执行
     *
     * @param list
     * @param batchSize
     * @param consumer
     * @return
     */
    default boolean executeBatch(Collection<T> list, int batchSize, BiConsumer<SqlSession, T> consumer) {
        Assert.isFalse(batchSize < 1, "batchSize must not be less than one");
        return !CollectionUtils.isEmpty(list) && this.executeBatch((sqlSession) -> {
            int size = list.size();
            int i = 1;
            for(Iterator<T> var6 = list.iterator(); var6.hasNext(); ++i) {
                T element = var6.next();
                consumer.accept(sqlSession, element);
                if (i % batchSize == 0 || i == size) {
                    sqlSession.flushStatements();
                }
            }
        });
    }

    /**
     * 批量执行
     *
     * @param consumer
     * @return
     */
    default boolean executeBatch(Consumer<SqlSession> consumer) {
        SqlSessionFactory sqlSessionFactory = SqlHelper.sqlSessionFactory(currentModelClass());
        SqlSessionHolder sqlSessionHolder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sqlSessionFactory);
        boolean transaction = TransactionSynchronizationManager.isSynchronizationActive();
        if (sqlSessionHolder != null) {
            SqlSession sqlSession = sqlSessionHolder.getSqlSession();
            //原生无法支持执行器切换，当存在批量操作时，会嵌套两个session的，优先commit上一个session
            //按道理来说，这里的值应该一直为false。
            sqlSession.commit(!transaction);
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        if (!transaction) {
            log.warn("SqlSession [" + sqlSession + "] was not registered for synchronization because DataSource is not transactional");
        }
        try {
            consumer.accept(sqlSession);
            //非事物情况下，强制commit。
            sqlSession.commit(!transaction);
            return true;
        } catch (Throwable t) {
            sqlSession.rollback();
            Throwable unwrapped = ExceptionUtil.unwrapThrowable(t);
            if (unwrapped instanceof RuntimeException) {
                MyBatisExceptionTranslator myBatisExceptionTranslator
                        = new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true);
                throw Objects.requireNonNull(myBatisExceptionTranslator.translateExceptionIfPossible((RuntimeException) unwrapped));
            }
            throw ExceptionUtils.mpe(unwrapped);
        } finally {
            sqlSession.close();
        }
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
