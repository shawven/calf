package com.github.shawven.calf.payment.config;

import com.github.shawven.calf.payment.PaymentManager;
import com.github.shawven.calf.payment.PaymentManagerImpl;
import com.github.shawven.calf.payment.PaymentOperations;
import com.github.shawven.calf.payment.properties.AppProperties;
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
@Import({AppProperties.class, AlipayConfiguration.class, WechatConfiguration.class, UnionpayConfiguration.class})
public class AppConfiguration {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private List<PaymentOperations> providers;

    @Bean
    @ConditionalOnMissingBean
    public PaymentManager paymentManager() {
        return new PaymentManagerImpl(providers, appProperties);
    }
}
