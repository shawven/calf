
package com.test.security.validate;

import com.test.security.validate.message.MessageTemplate;
import com.test.security.validate.property.ValidationConstants;
import com.test.security.validate.property.ValidationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.test.security.validate.ValidateCodeType.IMAGE;
import static com.test.security.validate.ValidateCodeType.SMS;

/**
 * 生成校验码的请求处理器
 */
@RestController
public class ValidateCodeController {

	@Autowired
	private ValidateCodeProcessorHolder validateCodeProcessorHolder;

    /**
     * 获取图形验证码
     *
     * @param request
     * @param response
     */
	@RequestMapping(ValidationConstants.DEFAULT_VALIDATE_CODE_URL_PREFIX + "/image")
	public void createImageCode(HttpServletRequest request, HttpServletResponse response){
        getValidateCodeProcessor(IMAGE).create(new ServletWebRequest(request, response));
	}

    /**
     * 给指定手机号发送短信验证码
     *
     * @param request
     * @param response
     */
    @RequestMapping(ValidationConstants.DEFAULT_VALIDATE_CODE_URL_PREFIX + "/sms")
    public void createRestrictedSmsCode(HttpServletRequest request, HttpServletResponse response){
        request.setAttribute(ValidationConstants.DEFAULT_ATTR_NAME_SMS_MESSAGE, MessageTemplate.DEFAULT_SMS_CODE);
        getValidateCodeProcessor(SMS).create(new ServletWebRequest(request, response));
    }

    /**
     * 创建验证码，根据验证码类型不同，调用不同的 {@link ValidateCodeProcessor}接口实现
     *
     * @param type
     */
	private ValidateCodeProcessor getValidateCodeProcessor(ValidateCodeType type) {
        return validateCodeProcessorHolder.findValidateCodeProcessor(type);
    }

}
