package com.test.payment.config;

import com.test.payment.PaymentOperations;
import com.test.payment.supplier.alipay.AlipayTemplate;
import com.test.payment.properties.AlipayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-08-28
 */
@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayConfiguration {

    @Autowired
    private AlipayProperties alipayProperties;

    @Bean
    public PaymentOperations alipayPcPaymentProvider() {
        return new AlipayTemplate.Web(alipayProperties);
    }

    @Bean
    public PaymentOperations alipayWapPaymentProvider() {
        return new AlipayTemplate.Wap(alipayProperties);
    }
}
