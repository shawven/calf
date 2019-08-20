
package com.test.security.validate.sms;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.security.base.ResponseData;
import com.test.security.validate.*;
import com.test.security.validate.message.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

import static com.test.security.validate.property.ValidationConstants.DEFAULT_ATTR_NAME_SMS_MESSAGE;
import static com.test.security.validate.property.ValidationConstants.DEFAULT_PARAMETER_NAME_MOBILE;

/**
 * 短信验证码处理器
 */
public class SmsCodeProcessor extends AbstractValidateCodeProcessor<ValidateCode> {

    private static final Logger logger = LoggerFactory.getLogger(SmsCodeProcessor.class);

	/**
	 * 短信验证码发送器
	 */
	private SmsCodeSender smsCodeSender;

    private ObjectMapper objectMapper ;

    public SmsCodeProcessor(ValidateCodeRepository validateCodeRepository,
                            ValidateCodeGenerator validateCodeGenerator,
                            SmsCodeSender smsCodeSender) {
        super(validateCodeRepository, validateCodeGenerator);
        this.objectMapper = new ObjectMapper();
        this.smsCodeSender = smsCodeSender;
    }

    @Override
	protected void send(ServletWebRequest webRequest, ValidateCode validateCode) {
        HttpServletRequest request = webRequest.getRequest();
        SmsCode smsCode = (SmsCode) validateCode;
        smsCode.setMobile(getMobile(request));

        String messageTemplate = (String) request.getAttribute(DEFAULT_ATTR_NAME_SMS_MESSAGE);
        smsCode.setMessage(new SmsMessage(messageTemplate, smsCode).toString());

        try {
            smsCodeSender.send(smsCode);
            responseMessage(webRequest, validateCode.getExpireIn() + "");
        } catch (ValidateCodeException e) {
            responseErrorMessage(webRequest.getResponse(), e.getMessage(), HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            responseErrorMessage(webRequest.getResponse(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
	}

    private void responseMessage(ServletWebRequest request, String message) {
        try {
            ResponseData result = new ResponseData(message);
            HttpServletResponse response = request.getResponse();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(200);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void responseErrorMessage(HttpServletResponse response, String message, int status) {
        try {
            ResponseData result = new ResponseData(status, message);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(status);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

	private String getMobile(HttpServletRequest request) {
        String mobile = request.getParameter(DEFAULT_PARAMETER_NAME_MOBILE);
        return Objects.isNull(mobile) ? String.valueOf(request.getAttribute(DEFAULT_PARAMETER_NAME_MOBILE)) : mobile;
    }

    public void setSmsCodeSender(SmsCodeSender smsCodeSender) {
        this.smsCodeSender = smsCodeSender;
    }
}
