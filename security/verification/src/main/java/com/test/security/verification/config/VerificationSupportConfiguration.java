package com.test.security.verification.config;

import com.test.security.verification.VerificationFilter;
import com.test.security.verification.VerificationProcessor;
import com.test.security.verification.VerificationProcessorHolder;
import com.test.security.verification.VerificationRepository;
import com.test.security.verification.captcha.CaptchaGenerator;
import com.test.security.verification.captcha.CaptchaProcessor;
import com.test.security.verification.properties.VerificationProperties;
import com.test.security.verification.sms.DefaultSmsSender;
import com.test.security.verification.sms.SmsGenerator;
import com.test.security.verification.sms.SmsProcessor;
import com.test.security.verification.sms.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Configuration
@EnableConfigurationProperties(VerificationProperties.class)
public class VerificationSupportConfiguration {

    @Autowired
    private VerificationProperties verificationProperties;

    /**
     * 短信验证码生成器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public SmsGenerator smsCodeGenerator() {
        return new SmsGenerator(verificationProperties);
    }

    /**
     * 短信验证码发送器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public SmsSender smsCodeSender() {
        return new DefaultSmsSender();
    }

    /**
     * 图片验证码图片生成器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaGenerator imageCodeGenerator() {
        return new CaptchaGenerator(verificationProperties);
    }

    /**
     * 短信验证码处理器
     *
     * @param verificationRepository
     * @param smsGenerator
     * @param smsSender
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(VerificationRepository.class)
    public SmsProcessor smsCodeProcessor(VerificationRepository verificationRepository,
                                         SmsGenerator smsGenerator,
                                         SmsSender smsSender) {
        return new SmsProcessor(verificationRepository, smsGenerator, smsSender);
    }

    /**
     * 图形验证码处理器
     *
     * @param verificationRepository
     * @param captchaGenerator
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(VerificationRepository.class)
    public CaptchaProcessor imageCodeProcessor(VerificationRepository verificationRepository,
                                               CaptchaGenerator captchaGenerator) {
        return new CaptchaProcessor(verificationRepository, captchaGenerator);
    }

    /**
     * 验证码处理器持有者
     *
     * @param processors
     * @return
     */
    @Bean
    @ConditionalOnBean(VerificationProcessor.class)
    public VerificationProcessorHolder verificationProcessorHolder(Map<String, VerificationProcessor> processors) {
        return new VerificationProcessorHolder(processors);
    }

    /**
     * 验证码校验过滤器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public VerificationFilter verificationFilter(VerificationProcessorHolder verificationProcessorHolder) {
        return new VerificationFilter(verificationProcessorHolder, verificationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public VerificationSecurityConfig verificationSecurityConfig(VerificationFilter filter) {
        return new VerificationSecurityConfig(filter);
    }
}

