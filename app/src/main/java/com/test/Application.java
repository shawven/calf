package com.test;

import com.google.common.util.concurrent.Service;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@MapperScan("com.test.mappers")
@ComponentScan(
        excludeFilters = @Filter(type = FilterType.REGEX,
                pattern = {"com.test.websocket.*"})
)
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
