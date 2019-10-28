package com.starter.security.verification.sms;

import com.starter.security.verification.Verification;

import java.time.LocalDateTime;

/**
 * @author Shoven
 * @date 2019-08-16
 */
public class Sms extends Verification {

    private static final long serialVersionUID = -4629437541504397425L;

    private String phone;

    private String message;

    public Sms(String code, int expireIn) {
        super(code, expireIn);
    }

    public Sms(String code, LocalDateTime expireTime) {
        super(code, expireTime);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
