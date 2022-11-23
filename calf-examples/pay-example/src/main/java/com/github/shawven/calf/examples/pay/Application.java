package com.github.shawven.calf.examples.pay;

import com.github.shawven.calf.payment.anotation.EnablePaymentSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@MapperScan("com.github.shawven.calf.demo.mapper")
@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX, pattern = {"com.github.shawven.calf.app.websocket.*"}))
@EnableAsync
@EnableAspectJAutoProxy
@EnablePaymentSupport
@EnableWebSocket
@EnableScheduling
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
