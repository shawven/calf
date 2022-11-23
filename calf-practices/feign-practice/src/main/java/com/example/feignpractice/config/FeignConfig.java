package com.example.feignpractice.config;

import feign.Client;
import feign.Contract;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import java.util.ArrayList;
import java.util.List;

/**
 * @see FeignClientsConfiguration
 * @see FeignAutoConfiguration
 */
@Configuration
public class FeignConfig {

	@Autowired(required = false)
	private List<AnnotatedParameterProcessor> parameterProcessors = new ArrayList<>();

    @Autowired(required = false)
    private List<FeignFormatterRegistrar> feignFormatterRegistrars = new ArrayList<>();

	@Autowired(required = false)
	private Environment env;

    @Bean
    public Client feignClient(HttpClient httpClient) {
        ApacheHttpClient client = new ApacheHttpClient(httpClient);
        return new DelegateClient(client);
    }


    /**
     * 自定义注解生效  DefaultRequest
     * @see DefaultRequest
     *
     * @param feignConversionService
     * @return
     */
	@Bean
	public Contract feignContract(ConversionService feignConversionService) {
		return new SpringMvcExtContract(this.parameterProcessors, feignConversionService, env);
	}

    @Bean
    public FormattingConversionService feignConversionService() {
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        for (FeignFormatterRegistrar feignFormatterRegistrar : feignFormatterRegistrars) {
            feignFormatterRegistrar.registerFormatters(conversionService);
        }
        return conversionService;
    }
}
