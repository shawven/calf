package com.starter.log.config;

import com.starter.log.RequestLogTaskCreator;
import com.starter.log.repository.DatabaseRepository;
import com.starter.log.repository.Slf4jRepository;
import com.starter.log.RequestRecordBuilder;
import com.starter.log.core.JoinPointExtractor;
import com.starter.log.core.JoinPointInfoExtractor;
import com.starter.log.core.LogRepository;
import com.starter.log.core.RecordBuilder;
import com.starter.log.core.LogTaskCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    public RecordBuilder recordBuilder() {
        return new RequestRecordBuilder();
    }


    @Bean
    @ConditionalOnMissingBean
    public JoinPointExtractor joinPointExtractor() {
        return new JoinPointInfoExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogTaskCreator logTaskCreator(JoinPointExtractor joinPointExtractor,
                                         List<LogRepository> repositories,
                                         RecordBuilder recordBuilder) {
        return new RequestLogTaskCreator(joinPointExtractor, repositories, recordBuilder);
    }
}
