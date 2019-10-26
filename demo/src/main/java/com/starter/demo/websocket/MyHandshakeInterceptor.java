package com.starter.demo.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

/**
 * @author FS
 * @date 2018-09-27 13:47
 */
public class MyHandshakeInterceptor extends HttpSessionHandshakeInterceptor {


    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        super.beforeHandshake(request, response, wsHandler, attributes);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if ("anonymousUser".equals(authentication.getPrincipal().toString())) {
            User user = new User("guest", "", true, true, true, true, authentication.getAuthorities());
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user,
                    authentication.getCredentials(), authentication.getAuthorities());
            attributes.put("token", token);
        }

        return true;
    }
}
