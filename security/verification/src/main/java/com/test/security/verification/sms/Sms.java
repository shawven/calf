package com.test.security.verification.sms;

import com.test.security.verification.Verification;

import java.time.LocalDateTime;

/**
 * @author Shoven
 * @date 2019-08-16
 */
public class Sms extends Verification {

    private static final long serialVersionUID = -4629437541504397425L;

    private String mobile;

    private String message;

    public Sms(String code, int expireIn) {
        super(code, expireIn);
    }

    public Sms(String code, LocalDateTime expireTime) {
        super(code, expireTime);
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
