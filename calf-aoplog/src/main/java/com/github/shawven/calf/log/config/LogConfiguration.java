package com.github.shawven.calf.log.config;

import com.github.shawven.calf.log.core.LogBuilder;
import com.github.shawven.calf.log.core.LogMetaCreator;
import com.github.shawven.calf.log.core.LogRepository;
import com.github.shawven.calf.log.repository.DatabaseRepository;
import com.github.shawven.calf.log.repository.Slf4jRepository;
import com.github.shawven.calf.log.RequestLogMetaCreator;
import com.github.shawven.calf.log.RequestLogMeta;
import com.github.shawven.calf.log.RequestLogBuilder;
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
