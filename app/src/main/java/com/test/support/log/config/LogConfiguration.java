package com.test.support.log.config;

import com.test.support.log.core.JoinPointInfoExtractor;
import com.test.support.log.RequestRecordBuilder;
import com.test.support.log.RequestLogTaskCreator;
import com.test.support.log.core.JoinPointExtractor;
import com.test.support.log.core.LogRepository;
import com.test.support.log.core.RecordBuilder;
import com.test.support.log.core.LogTaskCreator;
import com.test.support.log.repository.DatabaseRepository;
import com.test.support.log.repository.Slf4jRepository;
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
    public LogRepository slf4jRepository() {
        return new Slf4jRepository();
    }

    @Bean
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
