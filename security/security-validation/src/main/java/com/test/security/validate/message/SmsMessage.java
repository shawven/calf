package com.test.security.validate.message;

import com.test.security.validate.ValidateCode;

import java.text.MessageFormat;

/**
 * 短信消息
 *
 * @author Shoven
 * @date 2019-08-16
 */
public class SmsMessage {

    private String templateString;

    private ValidateCode validateCode;

    public SmsMessage(String templateString, ValidateCode validateCode) {
        this.templateString = templateString;
        this.validateCode = validateCode;
    }

    @Override
    public String toString() {
        int seconds = validateCode.getExpireIn() / 60;
        return MessageFormat.format(templateString, validateCode.getCode(), seconds);
    }
}
