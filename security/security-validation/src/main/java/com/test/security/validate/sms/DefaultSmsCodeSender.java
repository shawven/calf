
package com.test.security.validate.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的短信验证码发送器
 */
public class DefaultSmsCodeSender implements SmsCodeSender {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void send(SmsCode smsCode) {
		logger.warn("请配置真实的短信验证码发送器(SmsCodeSender)");
		logger.info("向手机[ "+ smsCode.getMobile() + "]发送短信["+ smsCode.getMessage()+"]");
	}

}
