package com.starter.log.core;

import com.starter.log.emun.LogAttribute;
import com.starter.log.emun.LogType;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-07-25 18:26
 */
public abstract class AbstractLogTask<T extends RecordMeta> implements LogTask {

    private static Logger logger = LoggerFactory.getLogger(LogLogAspect.class);

    private List<LogRepository> repositories;

    private RecordBuilder<T> recordBuilder;

    private JoinPoint joinPoint;

    private Throwable cause;

    private Object value;

    private long cost;

    public AbstractLogTask(List<LogRepository> repositories,
                           RecordBuilder<T> recordBuilder,
                           JoinPoint joinPoint) {
        this.repositories = repositories;
        this.recordBuilder = recordBuilder;
        this.joinPoint = joinPoint;
    }

    @Override
    public void run() {
        try {
            // 提取切入点信息
            JoinPointInfo joinPointInfo = new JoinPointInfo(joinPoint);
            // 生成日志元数据
            T recordMeta = makeRecordMeta(joinPointInfo);
            // 生成日志
            Recordable record = makeRecord(recordMeta);
            // 写入
            write(record, joinPointInfo);
        } catch (LogException e) {
            logger.warn(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(String.format("日志系统异常: %s", e.getMessage()), e);
        }
    }

    protected Recordable makeRecord(T recordMeta) {
        JoinPointInfo joinPointInfo = recordMeta.getJoinPointInfo();
        Class<? extends Annotation> aClass = joinPointInfo.getLog().getClass();
        Class<?> typeClass = joinPointInfo.getTypeClass();
        Method method = joinPointInfo.getMethod();

        Map<LogAttribute, Object> attributes= LogAnnotationUtils.getAnnotationAttributes(typeClass, method, aClass);
        Recordable record= recordBuilder.build(recordMeta);

        record.setModule(String.valueOf(attributes.get(LogAttribute.MODULE)));
        record.setDesc(String.valueOf(attributes.get(LogAttribute.VALUE)));
        record.setLogType(LogType.valueOf(String.valueOf(attributes.get(LogAttribute.TYPE))));
        return record;
    }

    @Override
    public void write(Recordable record, JoinPointInfo joinPointInfo) {
        int repositoriesSize = repositories.size();
        Map<String, Exception> exceptions = new HashMap<>(repositoriesSize);

        for (LogRepository repository : repositories) {
            if (repository.isSupport(joinPointInfo)) {
                try {
                    repository.write(record);
                } catch (Exception e) {
                    exceptions.put(repository.getClass().getSimpleName(), e);
                }
            }
        }

        int exceptionSize = exceptions.size();
        if (exceptionSize == repositoriesSize) {
            throw new RuntimeException(String.format("日志未保存：%s", exceptions.toString()));
        } else if (exceptions.size() > 0) {
            String message;
            if (exceptionSize == 1) {
                Map.Entry<String, Exception> onlyOne = exceptions.entrySet().iterator().next();
                message = String.format("日志[%s]保存失败：%s", onlyOne.getKey(), onlyOne.getValue());
            } else {
                message = String.format("以下日志保存失败：%s", exceptions.toString());
            }
            throw new LogException(message);
        }
    }

    protected abstract T makeRecordMeta(JoinPointInfo joinPointInfo);

    public List<LogRepository> getRepositories() {
        return repositories;
    }

    public RecordBuilder<T> getRecordBuilder() {
        return recordBuilder;
    }

    public JoinPoint getJoinPoint() {
        return joinPoint;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }
}
