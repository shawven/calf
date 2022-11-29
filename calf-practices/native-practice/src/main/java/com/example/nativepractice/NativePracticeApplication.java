package com.example.nativepractice;

import com.example.nativepractice.util.Spring;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableScheduling
@SpringBootApplication
public class NativePracticeApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(NativePracticeApplication.class, args);
        Spring.init(context);
    }

}
