package com.github.shawven.calf.practices.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import static org.springframework.messaging.simp.SimpMessageType.*;

/**
 * @author FS
 * @date 2018-09-26 16:28
 */
@Configuration
@EnableWebSocketMessageBroker
public class MyWebSocketConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages.nullDestMatcher().permitAll()
                .simpSubscribeDestMatchers("/user/**").permitAll()
                .simpDestMatchers("/app/**").permitAll()
                .simpTypeMatchers(
                        CONNECT,
                        CONNECT_ACK,
                        MESSAGE,
                        SUBSCRIBE,
                        UNSUBSCRIBE,
                        HEARTBEAT,
                        DISCONNECT,
                        DISCONNECT_ACK,
                        OTHER).permitAll()
                .anyMessage().permitAll();
    }

    /**
     * 将"/websocket"路径注册为STOMP端点，这个路径与发送和接收消息的目的路径有所不同，这是一个端点，客户端在订阅或发布消息到目的地址前，要连接该端点，
     * 即用户发送请求url="/hello"与STOMP server进行连接。之后再转发到订阅url；
     * PS：端点的作用——客户端在订阅或发布消息到目的地址前，要连接该端点。
     *
     * @param stompEndpointRegistry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        //在网页上可以通过"/applicationName/hello"来和服务器的WebSocket连接
        stompEndpointRegistry
                .addEndpoint("/websocket")
                .setHandshakeHandler(myHandler())
                .addInterceptors(myInterceptor())
                .setAllowedOrigins("*")
                .withSockJS();
    }

    /**
     * 配置了一个简单的消息代理，用来处理以"/topic"和"/user"为前缀的消息。
     * 消息代理将会处理前缀为"/topic"和"/user"的消息。
     *
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //应用程序以/app为前缀，代理目的地以/topic、/user为前缀
        registry.enableSimpleBroker("/topic", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Bean
    public HandshakeHandler myHandler() {
        return new MyHandshakeHandler();
    }

    @Bean
    public HandshakeInterceptor myInterceptor() {
        return new MyHandshakeInterceptor();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}

