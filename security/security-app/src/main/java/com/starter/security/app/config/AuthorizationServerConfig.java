
package com.starter.security.app.config;

import com.starter.security.oauth2.properties.OAuth2ClientProperties;
import com.starter.security.oauth2.properties.OAuth2Constants;
import com.starter.security.oauth2.properties.OAuth2Properties;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

/**
 * 认证服务器配置
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private TokenStore tokenStore;

	@Autowired(required = false)
	private JwtAccessTokenConverter jwtAccessTokenConverter;

	@Autowired(required = false)
	private TokenEnhancer jwtTokenEnhancer;

	@Autowired
	private OAuth2Properties oAuth2Properties;

	@Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Filter clientAuthenticationFilter;

    @Autowired
    private AccessDeniedHandler appAccessDeniedHandler;

    @Autowired
    private AuthenticationEntryPoint appOAuth2AuthenticationExceptionEntryPoint;

	/**
	 * 认证及token配置
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(tokenStore)
				.authenticationManager(authenticationManager)
				.userDetailsService(userDetailsService)
                .pathMapping("/oauth/token", OAuth2Constants.DEFAULT_OAUTH_TOKEN_ENDPOINTS);

		if (jwtAccessTokenConverter != null && jwtTokenEnhancer != null) {
            jwtAccessTokenConverter.setSigningKey(oAuth2Properties.getJwtSigningKey());

            TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
            List<TokenEnhancer> enhancers = new ArrayList<>();
            enhancers.add(jwtTokenEnhancer);
            enhancers.add(jwtAccessTokenConverter);
            enhancerChain.setTokenEnhancers(enhancers);

			endpoints
                    .tokenEnhancer(enhancerChain)
                    .accessTokenConverter(jwtAccessTokenConverter);
		}

	}

	/**
	 * tokenKey的访问权限表达式配置
	 */
	@Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("isAuthenticated()")
                .checkTokenAccess("isAuthenticated()")
                .accessDeniedHandler(appAccessDeniedHandler)
                .authenticationEntryPoint(appOAuth2AuthenticationExceptionEntryPoint)
                .addTokenEndpointAuthenticationFilter(clientAuthenticationFilter);
	}

	/**
	 * 客户端配置
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		InMemoryClientDetailsServiceBuilder builder = clients.inMemory();

		if (ArrayUtils.isNotEmpty(oAuth2Properties.getClients())) {
			for (OAuth2ClientProperties client : oAuth2Properties.getClients()) {
				builder.withClient(client.getClientId())
						.secret(passwordEncoder.encode(client.getClientSecret()))
						.authorizedGrantTypes("password", "authorization_code", "refresh_token", "client_credentials")
						.accessTokenValiditySeconds(client.getAccessTokenValidateSeconds())
						.refreshTokenValiditySeconds(client.getRefreshTokenValidateSeconds())
                        .autoApprove(true)
                        .scopes("all");
			}
		}
	}

}
