package com.test.payment.config;

import com.test.payment.PaymentOperations;
import com.test.payment.properties.UnionpayProperties;
import com.test.payment.supplier.unionpay.UnionpayTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-09-19
 */
@Configuration
@EnableConfigurationProperties(UnionpayProperties.class)
public class UnionpayConfiguration {

    @Autowired
    private UnionpayProperties unionpayProperties;

    @Bean
    public PaymentOperations unionpayPcPaymentProvider() {
        UnionpayTemplate.Web web = new UnionpayTemplate.Web();
        web.setProperties(unionpayProperties);
        return web;
    }

    @Bean
    public PaymentOperations unionpayWapPaymentProvider() {
        UnionpayTemplate.Wap wap = new UnionpayTemplate.Wap();
        wap.setProperties(unionpayProperties);
        return wap;
    }
}
