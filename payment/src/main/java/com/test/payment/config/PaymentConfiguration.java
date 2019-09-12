package com.test.payment.config;

import com.test.payment.PaymentOperations;
import com.test.payment.PaymentManagerImpl;
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
@Import({AlipayConfiguration.class, WechatConfiguration.class})
public class PaymentConfiguration {

    @Autowired
    private List<PaymentOperations> providers;

    @Bean
    @ConditionalOnMissingBean
    public PaymentManagerImpl paymentProviderManager() {
        return new PaymentManagerImpl(providers);
    }
}
