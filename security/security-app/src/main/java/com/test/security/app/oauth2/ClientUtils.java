package com.test.security.app.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.security.core.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Shoven
 * @since 2019-04-19 16:15
 */
public class ClientUtils {

    public static void authenticateClient(PasswordEncoder passwordEncoder, ClientDetails client,
                                             String[] clientDetail) {
        if (client == null) {
            throw new NoSuchClientException("客户端或密码错误");
        }
        if (!passwordEncoder.matches(clientDetail[1], client.getClientSecret())) {
            throw new BadCredentialsException("客户端或密码错误");
        }
    }

    public static void outputBadCredentials(HttpServletResponse response, Exception e) throws IOException {
        ResponseData objectResponse = new ResponseData()
                .setCode(HttpStatus.UNAUTHORIZED.value())
                .setMessage(e.getMessage());

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json,charset=utf-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(objectResponse));
    }

    public static String[] getClientInfo(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Basic ")) {
            throw new UnapprovedClientAuthenticationException("请求头Authorization中无客户端信息");
        }

        byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
        byte[] decoded;

        try {
            decoded = Base64.getDecoder().decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("不能解析客户端信息");
        }

        String token = new String(decoded, StandardCharsets.UTF_8);

        int delim = token.indexOf(":");
        if (delim == -1) {
            String id = request.getParameter("client_id");
            String secret = request.getParameter("client_secret");

            if (id == null || secret == null) {
                return null;
            }
            return new String[]{id, secret};
        }

        return new String[] {token.substring(0, delim), token.substring(delim + 1) };
    }

    public static void outputAbsentClient(HttpServletResponse response) throws IOException {
        outputErrorInfo(response, HttpStatus.BAD_REQUEST, "请求中未包含正确的客户端信息");
    }

    public static void outputErrorInfo(HttpServletResponse response, HttpStatus httpStatus, String message)
            throws IOException {
        ResponseData objectResponse = new ResponseData()
                .setCode(httpStatus.value())
                .setMessage(message);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json,charset=utf-8");
        response.setStatus(httpStatus.value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(objectResponse));
    }
}
