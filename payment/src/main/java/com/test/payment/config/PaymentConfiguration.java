package com.test.payment.config;

import com.test.payment.PaymentOperations;
import com.test.payment.PaymentManagerImpl;
import com.test.payment.properties.PaymentProperties;
import com.test.payment.properties.UnionpayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-08-28
 */
@Configuration
@Import({PaymentProperties.class, AlipayConfiguration.class,
        WechatConfiguration.class, UnionpayConfiguration.class})
public class PaymentConfiguration {

    @Autowired
    private PaymentProperties paymentProperties;

    @Autowired
    private List<PaymentOperations> providers;

    @Bean
    @ConditionalOnMissingBean
    public PaymentManagerImpl paymentProviderManager() {
        return new PaymentManagerImpl(providers, paymentProperties);
    }
}
