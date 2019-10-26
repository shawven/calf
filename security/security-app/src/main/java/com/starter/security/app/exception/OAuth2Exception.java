package com.starter.security.app.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starter.security.app.oauth2.OAuthExceptionJacksonSerializer;

/**
 * @author Shoven
 * @date 2018/11/5 18:00
 */
@JsonSerialize(using = OAuthExceptionJacksonSerializer.class)
public class OAuth2Exception extends org.springframework.security.oauth2.common.exceptions.OAuth2Exception {

    public OAuth2Exception(String msg, Throwable t) {
        super(msg, t);
    }

    public OAuth2Exception(String msg) {
        super(msg);

    }
}
