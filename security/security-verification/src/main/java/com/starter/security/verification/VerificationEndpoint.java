
package com.starter.security.verification;

import com.starter.security.verification.message.MessageTemplate;
import com.starter.security.verification.properties.VerificationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.starter.security.verification.VerificationType.CAPTCHA;
import static com.starter.security.verification.VerificationType.SMS;

/**
 * 生成校验码的请求处理器
 */
@RestController
public class VerificationEndpoint {

	@Autowired
	private VerificationProcessorHolder verificationProcessorHolder;

    /**
     * 获取图形验证码
     *
     * @param request
     * @param response
     */
	@RequestMapping(VerificationConstants.DEFAULT_VERIFICATION_URL_PREFIX + "/captcha")
	public void createImageCode(HttpServletRequest request, HttpServletResponse response){
        getVerificationProcessor(CAPTCHA).create(new ServletWebRequest(request, response));
	}

    /**
     * 给指定手机号发送短信验证码
     *
     * @param request
     * @param response
     */
    @RequestMapping(VerificationConstants.DEFAULT_VERIFICATION_URL_PREFIX + "/sms")
    public void createRestrictedSmsCode(HttpServletRequest request, HttpServletResponse response){
        request.setAttribute(VerificationConstants.DEFAULT_ATTR_NAME_SMS_MESSAGE, MessageTemplate.DEFAULT_SMS_CODE);
        getVerificationProcessor(SMS).create(new ServletWebRequest(request, response));
    }

    /**
     * 创建验证码，根据验证码类型不同，调用不同的 {@link VerificationProcessor}接口实现
     *
     * @param type
     */
	private VerificationProcessor getVerificationProcessor(VerificationType type) {
        return verificationProcessorHolder.get(type);
    }

}
