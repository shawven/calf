package com.github.shawven.calf.examples.oauth2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.github.shawven.calf.oauth2.mapper")
@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX, pattern = {"com.github.shawven.calf.app.websocket.*"}))
@EnableAsync
@EnableAspectJAutoProxy
@EnableScheduling
@SpringBootApplication
public class Oauth2Application {

	public static void main(String[] args) {
		SpringApplication.run(Oauth2Application.class, args);
	}
}
