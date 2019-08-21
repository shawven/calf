package com.test.log.core;

import com.test.log.annotation.Log;
import com.test.log.emun.LogAttribute;
import com.test.log.emun.LogType;
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
public abstract class AbstractLogTask implements LogTask {

    private static Logger logger = LoggerFactory.getLogger(LogLogAspect.class);

    private JoinPointExtractor joinPointExtractor;

    private List<LogRepository> repositories;

    private RecordBuilder recordBuilder;

    private JoinPoint joinPoint;

    public AbstractLogTask(JoinPointExtractor joinPointExtractor,
                           List<LogRepository> repositories,
                           RecordBuilder recordBuilder,
                           JoinPoint joinPoint) {
        this.joinPointExtractor = joinPointExtractor;
        this.repositories = repositories;
        this.recordBuilder = recordBuilder;
        this.joinPoint = joinPoint;
    }

    @Override
    public void run() {
        try {
            // 提取切入点信息
            JoinPointInfo joinPointInfo = joinPointExtractor.extract(joinPoint);
            // 生成日志元数据
            RecordMeta recordMeta = makeRecordMeta(joinPointInfo);
            // 生成日志
            Recordable record = makeRecord(recordMeta);
            // 写入
            write(record, recordMeta.getJoinPointInfo().getLogAnnotation());
        } catch (LogException e) {
            logger.warn(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(String.format("日志系统异常: %s", e.getMessage()), e);
        }
    }

    protected Recordable makeRecord(RecordMeta recordMeta) {
        JoinPointInfo joinPointInfo = recordMeta.getJoinPointInfo();
        Class<? extends Annotation> aClass = joinPointInfo.getLogAnnotation().getClass();
        Class typeClass = joinPointInfo.getTypeClass();
        Method method = joinPointInfo.getMethod();

        Map<LogAttribute, Object> attributes= LogAnnotationUtils.getAnnotationAttributes(typeClass, method, aClass);
        Recordable record= recordBuilder.build(recordMeta);

        record.setModule(String.valueOf(attributes.get(LogAttribute.MODULE)));
        record.setDesc(String.valueOf(attributes.get(LogAttribute.VALUE)));
        record.setLogType(LogType.valueOf(String.valueOf(attributes.get(LogAttribute.TYPE))));
        return record;
    }

    @Override
    public void write(Recordable record, Log annotation) {
        int repositoriesSize = repositories.size();
        Map<String, Exception> exceptions = new HashMap<>(repositoriesSize);

        for (LogRepository repository : repositories) {
            if (repository.isSupport(annotation)) {
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

    protected abstract RecordMeta makeRecordMeta(JoinPointInfo joinPointInfo);

    public JoinPointExtractor getJoinPointExtractor() {
        return joinPointExtractor;
    }

    public void setJoinPointExtractor(JoinPointExtractor joinPointExtractor) {
        this.joinPointExtractor = joinPointExtractor;
    }

    public List<LogRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<LogRepository> repositories) {
        this.repositories = repositories;
    }

    public RecordBuilder getRecordBuilder() {
        return recordBuilder;
    }

    public void setRecordBuilder(RecordBuilder recordBuilder) {
        this.recordBuilder = recordBuilder;
    }

    public JoinPoint getJoinPoint() {
        return joinPoint;
    }

    public void setJoinPoint(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }
}
