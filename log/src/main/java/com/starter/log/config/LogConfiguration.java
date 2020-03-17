package com.starter.log.config;

import com.starter.log.RequestLogMetaCreator;
import com.starter.log.RequestLogMeta;
import com.starter.log.repository.DatabaseRepository;
import com.starter.log.repository.Slf4jRepository;
import com.starter.log.RequestLogBuilder;
import com.starter.log.core.LogRepository;
import com.starter.log.core.LogBuilder;
import com.starter.log.core.LogMetaCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-07-26 15:20
 */
@Configuration
public class LogConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LogRepository slf4jRepository() {
        return new Slf4jRepository();
    }

    @Bean
    @ConditionalOnMissingBean()
    public LogRepository databaseRepository() {
        return new DatabaseRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogBuilder<RequestLogMeta> logBuilder() {
        return new RequestLogBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogMetaCreator<RequestLogMeta> logMetaCreator() {
        return new RequestLogMetaCreator();
    }
}
