package com.test.payment.config;

import com.test.payment.PaymentOperations;
import com.test.payment.supplier.wechat.WechatPayTemplate;
import com.test.payment.properties.WechatPayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-08-28
 */
@Configuration
@EnableConfigurationProperties(WechatPayProperties.class)
public class WechatConfiguration {

    @Autowired
    private WechatPayProperties wechatPayProperties;

    @Bean
    public PaymentOperations wechatPayPcOperations() {
        WechatPayTemplate.Web web = new WechatPayTemplate.Web();
        web.setProperties(wechatPayProperties);
        return web;
    }

    @Bean
    public PaymentOperations wechatPayWapOperations() {
        WechatPayTemplate.Wap wap = new WechatPayTemplate.Wap();
        wap.setProperties(wechatPayProperties);
        return wap;
    }
}
