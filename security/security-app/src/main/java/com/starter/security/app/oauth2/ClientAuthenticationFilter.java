package com.starter.security.app.oauth2;

import com.starter.security.social.properties.OAuth2Constants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 客户端认证过滤器，自带的无法控制响应格式
 *
 * @author Shoven
 * @date 2018/11/5 11:38
 */

public class ClientAuthenticationFilter extends OncePerRequestFilter {

    private ClientDetailsService clientDetailsService;

    private PasswordEncoder passwordEncoder;

    public ClientAuthenticationFilter(ClientDetailsService clientDetailsService, PasswordEncoder passwordEncoder) {
        this.clientDetailsService = clientDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!OAuth2Constants.DEFAULT_OAUTH_TOKEN_ENDPOINTS.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            filterChain.doFilter(request,response);
            return;
        }

        String[] clientInfo =  ClientUtils.getClientInfo(request);
        if (clientInfo == null) {
            ClientUtils.outputAbsentClient(response);
            return;
        }

        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientInfo[0]);
        try {
            ClientUtils.authenticateClient(passwordEncoder, clientDetails, clientInfo);
        } catch (Exception e) {
            ClientUtils.outputBadCredentials(response, e);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
