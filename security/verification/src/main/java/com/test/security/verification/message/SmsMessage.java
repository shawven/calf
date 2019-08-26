package com.test.security.verification.message;

import com.test.security.verification.Verification;

import java.text.MessageFormat;

/**
 * 短信消息
 *
 * @author Shoven
 * @date 2019-08-16
 */
public class SmsMessage {

    private String templateString;

    private Verification verification;

    public SmsMessage(String templateString, Verification verification) {
        this.templateString = templateString;
        this.verification = verification;
    }

    @Override
    public String toString() {
        int seconds = verification.getExpireIn() / 60;
        return MessageFormat.format(templateString, verification.getCode(), seconds);
    }
}
