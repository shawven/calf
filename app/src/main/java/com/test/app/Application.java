package com.test.app;

import com.test.payment.anotation.EnablePaymentSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@MapperScan("com.test.app.mapper")
@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX, pattern = {"com.test.app.websocket.*"}))
@EnablePaymentSupport
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
