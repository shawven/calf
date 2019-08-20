package com.test.security.validate.message;

/**
 * 消息模板
 *
 * @author Shoven
 * @date 2019-08-16
 */
public interface MessageTemplate {

    String DEFAULT_SMS_CODE = "【XXX】短信验证码：{0}";

    String WITH_EXPIRE_TIME_SMS_CODE = "【XXX】短信验证码：{0}，{1,number}分钟内输入有效";
}
