package com.starter.demo.support.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.starter.demo.support.CtxDataAccessor;
import com.starter.demo.support.RememberMeAccessor;
import com.starter.demo.support.ContextInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Shoven
 * @date 2019-10-31
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${app.security.whitelist}")
    private String whitelist;

    private CtxDataAccessor ctxDataAccessor;

    private RememberMeAccessor rememberMeAccessor;

    public WebMvcConfiguration(CtxDataAccessor ctxDataAccessor,
                               RememberMeAccessor rememberMeAccessor) {
        this.ctxDataAccessor = ctxDataAccessor;
        this.rememberMeAccessor = rememberMeAccessor;
    }

    /**
     * 表单参数过滤器
     * 解决mvc无法获取put请求的body参数问题
     *
     * @return OrderedFormContentFilter
     */
    @Bean
    public OrderedFormContentFilter formContentFilter() {
        return new OrderedFormContentFilter();
    }

    /**
     * 跨域支持
     *
     * @return FilterRegistrationBean<CorsFilter>
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(-9901);
        return bean;
    }

    /**
     * 用户上下文拦截器资源释放、重复请求校验
     *
     * @param registry InterceptorRegistry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        ContextInterceptor interceptor =
                new ContextInterceptor(ctxDataAccessor, rememberMeAccessor, profile);
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则，/**表示拦截所有请求
        // excludePathPatterns 用户排除拦截
        InterceptorRegistration registration = registry.addInterceptor(interceptor);
        registration.addPathPatterns("/**");
        // 排除用户授权端点
        registration.excludePathPatterns("/oauth/**");
        // 排除API白名单端点
        String[] list = StringUtils.split(whitelist, ",");
        if (list != null) {
            registration.excludePathPatterns(list);
        }
        super.addInterceptors(registry);
    }

    /**
     * 解决配置spring.jackson.date-format全局配置失效问题
     * 解决Js 不支持long类型问题，把long转成字符串
     *
     * @param converters List<HttpMessageConverter<?>> converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = converter.getObjectMapper();
        //属性为NULL不序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 时间格式化
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // long转字符串
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        converter.setObjectMapper(objectMapper);
        converters.add(0, converter);
    }
}
