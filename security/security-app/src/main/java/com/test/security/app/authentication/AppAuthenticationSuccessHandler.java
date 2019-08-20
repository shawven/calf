
package com.test.security.app.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.security.app.oauth2.ClientUtils;
import com.test.security.base.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * APP环境下认证成功处理器
 */
public class AppAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private ObjectMapper objectMapper;

    private PasswordEncoder passwordEncoder;

	private ClientDetailsService clientDetailsService;

	private AuthorizationServerTokenServices authorizationServerTokenServices;

    public AppAuthenticationSuccessHandler(ClientDetailsService clientDetailsService, PasswordEncoder passwordEncoder,
                                           AuthorizationServerTokenServices authorizationServerTokenServices) {
        this.objectMapper = new ObjectMapper();
        this.clientDetailsService = clientDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authorizationServerTokenServices = authorizationServerTokenServices;
    }

    @Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
        String[] clientInfo;

        try {
            clientInfo = ClientUtils.getClientInfo(request);
            if (clientInfo == null) {
                ClientUtils.outputAbsentClient(response);
                return;
            }
        } catch (RuntimeException e) {
            ClientUtils.outputErrorInfo(response, HttpStatus.UNAUTHORIZED, e.getMessage());
            return;
        }

		String clientId = clientInfo[0];
		ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);

        try {
            ClientUtils.authenticateClient(passwordEncoder, clientDetails, clientInfo);
        } catch (Exception e) {
            ClientUtils.outputBadCredentials(response, e);
        }

		TokenRequest tokenRequest = new TokenRequest(Collections.emptyMap(),
                clientId, clientDetails.getScope(), "custom");

		OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);

		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);

        ResponseData tokenResult;

        try {
            OAuth2AccessToken token = authorizationServerTokenServices.createAccessToken(oAuth2Authentication);

            String  message = "refresh_token".equals(request.getParameter("grant_type"))
                    ? "刷新token成功"
                    : "获取token成功";

            tokenResult = new ResponseData()
                    .setMessage(message)
                    .setData(token);

        } catch (AuthenticationException e) {
            tokenResult = new ResponseData()
                        .setCode(HttpStatus.UNAUTHORIZED.value())
                        .setMessage(e.getMessage());

        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(tokenResult));
	}



}
