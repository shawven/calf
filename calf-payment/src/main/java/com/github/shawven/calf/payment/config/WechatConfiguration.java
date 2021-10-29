package com.github.shawven.calf.payment.config;

import com.github.shawven.calf.payment.PaymentOperations;
import com.github.shawven.calf.payment.properties.WechatPayProperties;
import com.github.shawven.calf.payment.provider.wechat.WechatPayTemplate;
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
    public PaymentOperations wechatPayQrcOperations() {
        WechatPayTemplate.WebQrc webQrc = new WechatPayTemplate.WebQrc();
        webQrc.setProperties(wechatPayProperties);
        return webQrc;
    }

    @Bean
    public PaymentOperations wechatPayJsApiOperations() {
        WechatPayTemplate.JsApi jsApi = new WechatPayTemplate.JsApi();
        jsApi.setProperties(wechatPayProperties);
        return jsApi;
    }

    @Bean
    public PaymentOperations wechatPayWapOperations() {
        WechatPayTemplate.Wap wap = new WechatPayTemplate.Wap();
        wap.setProperties(wechatPayProperties);
        return wap;
    }

    @Bean
    public PaymentOperations wechatPayF2fOperations() {
        WechatPayTemplate.F2f f2f = new WechatPayTemplate.F2f();
        f2f.setProperties(wechatPayProperties);
        return f2f;
    }

    @Bean
    public PaymentOperations wechatPayAppOperations() {
        WechatPayTemplate.App app = new WechatPayTemplate.App();
        app.setProperties(wechatPayProperties);
        return app;
    }
}
