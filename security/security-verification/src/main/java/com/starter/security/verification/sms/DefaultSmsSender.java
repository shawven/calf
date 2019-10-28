
package com.starter.security.verification.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的短信验证码发送器
 */
public class DefaultSmsSender implements SmsSender {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void send(Sms sms) {
		logger.warn("请配置真实的短信验证码发送器(SmsCodeSender)");
		logger.info("向手机[ "+ sms.getPhone() + "]发送短信["+ sms.getMessage()+"]");
	}

}
