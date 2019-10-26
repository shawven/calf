
package com.starter.security.verification;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * 验证码信息封装类
 */
public class Verification implements Serializable {

	private static final long serialVersionUID = 1588203828504660915L;

	private String code;

	private int expireIn;

	private LocalDateTime expireTime;

	public Verification(String code, int expireIn){
		this.code = code;
		this.expireIn = expireIn;
		this.expireTime = LocalDateTime.now(ZoneOffset.of("+8")).plusSeconds(expireIn);
	}

	public Verification(String code, LocalDateTime expireTime){
		this.code = code;
        this.expireIn = (int)(expireTime.toEpochSecond(ZoneOffset.of("+8")) -
                LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
		this.expireTime = expireTime;
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expireTime);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

    public int getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(int expireIn) {
        this.expireIn = expireIn;
    }

    public LocalDateTime getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(LocalDateTime expireTime) {
		this.expireTime = expireTime;
	}

}
