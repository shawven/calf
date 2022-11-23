package com.github.shawven.calf.practices.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX, pattern = {"com.github.shawven.calf.app.websocket.*"}))
@EnableAsync
@EnableAspectJAutoProxy
@EnableWebSocket
@EnableScheduling
@SpringBootApplication
public class WebSocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebSocketApplication.class, args);
	}
}
