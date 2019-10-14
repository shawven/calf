package com.test.payment.config;

import com.test.payment.PaymentOperations;
import com.test.payment.properties.AlipayProperties;
import com.test.payment.provider.alipay.AlipayTemplate;
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
        AlipayTemplate.Web web = new AlipayTemplate.Web();
        web.setProperties(alipayProperties);
        return web;
    }

    @Bean
    public PaymentOperations alipayWapPaymentProvider() {
        AlipayTemplate.Wap wap = new AlipayTemplate.Wap();
        wap.setProperties(alipayProperties);
        return wap;
    }

    @Bean
    public PaymentOperations alipayWebQrcPaymentProvider() {
        AlipayTemplate.WebQrc webQrc = new AlipayTemplate.WebQrc();
        webQrc.setProperties(alipayProperties);
        return webQrc;
    }

    @Bean
    public PaymentOperations alipayF2fPaymentProvider() {
        AlipayTemplate.F2f f2f = new AlipayTemplate.F2f();
        f2f.setProperties(alipayProperties);
        return f2f;
    }

    @Bean
    public PaymentOperations alipayQrcPaymentProvider() {
        AlipayTemplate.Qrc qrc = new AlipayTemplate.Qrc();
        qrc.setProperties(alipayProperties);
        return qrc;
    }

    @Bean
    public PaymentOperations alipayAppPaymentProvider() {
        AlipayTemplate.App app = new AlipayTemplate.App();
        app.setProperties(alipayProperties);
        return app;
    }
}
