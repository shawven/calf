package com.starter.payment.config;

import com.starter.payment.PaymentManagerImpl;
import com.starter.payment.PaymentOperations;
import com.starter.payment.properties.GlobalProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-08-28
 */
@Configuration
@Import({GlobalProperties.class, AlipayConfiguration.class,
        WechatConfiguration.class, UnionpayConfiguration.class})
public class GlobalConfiguration {

    @Autowired
    private GlobalProperties globalProperties;

    @Autowired
    private List<PaymentOperations> providers;

    @Bean
    @ConditionalOnMissingBean
    public PaymentManagerImpl paymentProviderManager() {
        return new PaymentManagerImpl(providers, globalProperties);
    }
}
