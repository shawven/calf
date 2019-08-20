
package com.test.security.validate.sms;


public interface SmsCodeSender {


    /**
     * @param smsCode
     */
	void send(SmsCode smsCode);

}
