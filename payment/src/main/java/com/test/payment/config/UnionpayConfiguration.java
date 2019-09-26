package com.test.payment.config;

import com.test.payment.PaymentOperations;
import com.test.payment.properties.UnionpayB2BProperties;
import com.test.payment.properties.UnionpayB2CProperties;
import com.test.payment.properties.UnionpayProperties;
import com.test.payment.supplier.unionpay.UnionpayB2BTemplate;
import com.test.payment.supplier.unionpay.UnionpayB2CTemplate;
import com.test.payment.supplier.unionpay.UnionpayTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Shoven
 * @date 2019-09-19
 */
@Configuration
@EnableConfigurationProperties({UnionpayB2CProperties.class, UnionpayB2BProperties.class})
public class UnionpayConfiguration {

    @Autowired
    private UnionpayB2CProperties unionpayB2CProperties;

    @Autowired
    private UnionpayB2BProperties unionpayB2BProperties;

    @Bean
    public PaymentOperations unionpayPcB2CPaymentProvider() {
        UnionpayB2CTemplate.Web web = new UnionpayB2CTemplate.Web();
        web.setProperties(unionpayB2CProperties);
        return web;
    }

    @Bean
    public PaymentOperations unionpayWapB2CPaymentProvider() {
        UnionpayB2CTemplate.Wap wap = new UnionpayB2CTemplate.Wap();
        wap.setProperties(unionpayB2CProperties);
        return wap;
    }

    @Bean
    public PaymentOperations unionpayB2BPcPaymentProvider() {
        UnionpayB2BTemplate.Web web = new UnionpayB2BTemplate.Web();
        web.setProperties(unionpayB2BProperties);
        return web;
    }

    @Bean
    public PaymentOperations unionpayB2BWapPaymentProvider() {
        UnionpayB2BTemplate.Wap wap = new UnionpayB2BTemplate.Wap();
        wap.setProperties(unionpayB2BProperties);
        return wap;
    }
}
