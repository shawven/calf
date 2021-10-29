package com.github.shawven.calf.payment.config;

import com.github.shawven.calf.payment.PaymentOperations;
import com.github.shawven.calf.payment.properties.UnionpayB2BProperties;
import com.github.shawven.calf.payment.properties.UnionpayB2CProperties;
import com.github.shawven.calf.payment.provider.unionpay.UnionpayB2BTemplate;
import com.github.shawven.calf.payment.provider.unionpay.UnionpayB2CTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public PaymentOperations unionpayF2fB2CPaymentProvider() {
        UnionpayB2CTemplate.F2f f2f = new UnionpayB2CTemplate.F2f();
        f2f.setProperties(unionpayB2CProperties);
        return f2f;
    }

    @Bean
    public PaymentOperations unionpayQrcB2CPaymentProvider() {
        UnionpayB2CTemplate.Qrc qrc = new UnionpayB2CTemplate.Qrc();
        qrc.setProperties(unionpayB2CProperties);
        return qrc;
    }

    @Bean
    public PaymentOperations unionpayAppB2CPaymentProvider() {
        UnionpayB2CTemplate.App app = new UnionpayB2CTemplate.App();
        app.setProperties(unionpayB2CProperties);
        return app;
    }
}
