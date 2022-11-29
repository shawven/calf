package com.example.nativepractice.controller;

import com.alibaba.fastjson.JSON;
import com.example.nativepractice.util.Result;
import com.example.nativepractice.util.Spring;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * @author xw
 * @date 2022/11/19
 */
@RestController
public class IndexController {


    @GetMapping(value = "/")
    public Object home() {
        var i = 10;


        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("http://www.baidu.com"))
                .GET()
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("username", "password".toCharArray());
                    }
                })
                .connectTimeout(Duration.ofSeconds(10))
                .executor(Executors.newFixedThreadPool(10))
                .build();

        CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        future.thenAccept(response -> {
            System.out.println(response);
        });



        String x = "s";
        switch (x) {
            case "x" -> System.out.println("");
        }

        Object s = "x";
        if (s instanceof String str) {
            str.isBlank();
        }

        String json = """
                {
                    "id": "6365fdd3c041227bf48909ad",
                    "type": 1,
                    "grantAppIds": ["6359f7083ad05d0001a8d195"],
                    "editable": true
                }
                """;
        return Result.success(JSON.parse(json));
    }


    @RequestMapping(value = "/env")
    public Result<?> invoke() {
        return Result.success(System.getenv());
    }

    @RequestMapping(value = "/invoke")
    public Result<?> invoke(String spel) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(Spring.getContext()));
        ExpressionParser parser = new SpelExpressionParser();
        try {
            Object value = parser.parseExpression(spel).getValue(context);
            return Result.success(value);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}


