
package com.starter.security.verification.sms;

import com.starter.security.verification.properties.VerificationProperties;
import com.starter.security.verification.VerificationGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 短信验证码生成器
 */
public class SmsGenerator implements VerificationGenerator<Sms> {

	private com.starter.security.verification.properties.VerificationProperties VerificationProperties;

    public SmsGenerator(VerificationProperties VerificationProperties) {
        this.VerificationProperties = VerificationProperties;
    }

    @Override
	public Sms generate(ServletWebRequest request) {
		String code = RandomStringUtils.randomNumeric(VerificationProperties.getSms().getLength());
		return new Sms(code, VerificationProperties.getSms().getExpireIn());
	}

}
