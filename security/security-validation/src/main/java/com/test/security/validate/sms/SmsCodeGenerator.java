
package com.test.security.validate.sms;

import com.test.security.validate.ValidateCode;
import com.test.security.validate.ValidateCodeGenerator;
import com.test.security.validate.property.ValidationProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 短信验证码生成器
 */
public class SmsCodeGenerator implements ValidateCodeGenerator {

	private ValidationProperties validationProperties;

    public SmsCodeGenerator(ValidationProperties validationProperties) {
        this.validationProperties = validationProperties;
    }

    @Override
	public ValidateCode generate(ServletWebRequest request) {
		String code = RandomStringUtils.randomNumeric(validationProperties.getSms().getLength());
		return new SmsCode(code, validationProperties.getSms().getExpireIn());
	}

}
