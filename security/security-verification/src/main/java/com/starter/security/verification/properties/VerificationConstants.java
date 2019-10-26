
package com.starter.security.verification.properties;


/**
 * 验证码常量
 */
public interface VerificationConstants {

    /**
     * 默认的校验处理验证码的url前缀
     */
    String DEFAULT_VERIFICATION_URL_PREFIX = "/verification";

    /**
	 * 验证图片验证码时，http请求中默认的携带图片验证码信息的参数的名称
	 */
	String DEFAULT_PARAMETER_NAME_CAPTCHA = "imageCode";

	/**
	 * 验证短信验证码时，http请求中默认的携带短信验证码信息的参数的名称
	 */
	String DEFAULT_PARAMETER_NAME_SMS = "smsCode";

	/**
	 * 发送短信验证码 或 验证短信验证码时，传递手机号的参数的名称
	 */
	String DEFAULT_PARAMETER_NAME_MOBILE = "mobile";

    /**
     * 发送短信验证码 或 验证短信验证码时，传递手机号的参数的名称
     */
    String DEFAULT_ATTR_NAME_SMS_MESSAGE = "sms_message";
}
