package com.test.payment.config;

import com.test.payment.PaymentOperations;
import com.test.payment.properties.UnionpayProperties;
import com.test.payment.supplier.PaymentSupplierEnum;
import com.test.payment.supplier.unionpay.UnionpayB2BTemplate;
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

    @Bean
    public PaymentOperations unionpayB2BPcPaymentProvider() {
        UnionpayB2BTemplate.Web web = new UnionpayB2BTemplate.Web();
        web.setProperties(unionpayProperties);
        return web;
    }

    @Bean
    public PaymentOperations unionpayB2BWapPaymentProvider() {
        UnionpayB2BTemplate.Wap wap = new UnionpayB2BTemplate.Wap();
        wap.setProperties(unionpayProperties);
        return wap;
    }
}
