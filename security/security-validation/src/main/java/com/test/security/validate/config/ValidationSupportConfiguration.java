package com.test.security.validate.config;

import com.test.security.validate.ValidateCodeProcessor;
import com.test.security.validate.image.ImageCodeGenerator;
import com.test.security.validate.image.ImageCodeProcessor;
import com.test.security.validate.property.ValidationProperties;
import com.test.security.validate.sms.SmsCodeGenerator;
import com.test.security.validate.sms.SmsCodeProcessor;
import com.test.security.validate.sms.SmsCodeSender;
import com.test.security.validate.ValidateCodeFilter;
import com.test.security.validate.ValidateCodeProcessorHolder;
import com.test.security.validate.ValidateCodeRepository;
import com.test.security.validate.sms.DefaultSmsCodeSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Configuration
public class ValidationSupportConfiguration {

    @Autowired
    private ValidationProperties validationProperties;

    /**
     * 短信验证码生成器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public SmsCodeGenerator smsCodeGenerator() {
        return new SmsCodeGenerator(validationProperties);
    }

    /**
     * 短信验证码发送器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public SmsCodeSender smsCodeSender() {
        return new DefaultSmsCodeSender();
    }

    /**
     * 图片验证码图片生成器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ImageCodeGenerator imageCodeGenerator() {
        return new ImageCodeGenerator(validationProperties);
    }

    /**
     * 短信验证码处理器
     *
     * @param validateCodeRepository
     * @param smsCodeGenerator
     * @param smsCodeSender
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ValidateCodeRepository.class)
    public SmsCodeProcessor smsCodeProcessor(ValidateCodeRepository validateCodeRepository,
                                             SmsCodeGenerator smsCodeGenerator,
                                             SmsCodeSender smsCodeSender) {
        return new SmsCodeProcessor(validateCodeRepository, smsCodeGenerator, smsCodeSender);
    }

    /**
     * 图形验证码处理器
     *
     * @param validateCodeRepository
     * @param imageCodeGenerator
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ValidateCodeRepository.class)
    public ImageCodeProcessor imageCodeProcessor(ValidateCodeRepository validateCodeRepository,
                                                 ImageCodeGenerator imageCodeGenerator) {
        return new ImageCodeProcessor(validateCodeRepository, imageCodeGenerator);
    }

    /**
     * 验证码处理器持有者
     *
     * @param processors
     * @return
     */
    @Bean
    @ConditionalOnBean(ValidateCodeProcessor.class)
    public ValidateCodeProcessorHolder validateCodeProcessorHolder(Map<String, ValidateCodeProcessor> processors) {
        return new ValidateCodeProcessorHolder(processors);
    }

    /**
     * 验证码校验过滤器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ValidateCodeFilter validateCodeFilter(ValidateCodeProcessorHolder validateCodeProcessorHolder) {
        return new ValidateCodeFilter(validateCodeProcessorHolder, validationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidationSecurityConfig validateCodeSecurityConfig(ValidateCodeFilter filter) {
        return new ValidationSecurityConfig(filter);
    }
}

