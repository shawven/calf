
package com.test.security.verification.sms;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.security.base.ResponseData;
import com.test.app.security.verification.*;
import com.test.security.verification.*;
import com.test.security.verification.message.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

import static com.test.security.verification.properties.VerificationConstants.DEFAULT_ATTR_NAME_SMS_MESSAGE;
import static com.test.security.verification.properties.VerificationConstants.DEFAULT_PARAMETER_NAME_MOBILE;

/**
 * 短信验证码处理器
 */
public class SmsProcessor extends AbstractVerificationProcessor<Verification> {

    private static final Logger logger = LoggerFactory.getLogger(SmsProcessor.class);

	/**
	 * 短信验证码发送器
	 */
	private SmsSender smsSender;

    private ObjectMapper objectMapper ;

    public SmsProcessor(VerificationRepository verificationRepository,
                        VerificationGenerator verificationGenerator,
                        SmsSender smsSender) {
        super(verificationRepository, verificationGenerator);
        this.objectMapper = new ObjectMapper();
        this.smsSender = smsSender;
    }

    @Override
	protected void send(ServletWebRequest webRequest, Verification verification) {
        HttpServletRequest request = webRequest.getRequest();
        Sms sms = (Sms) verification;
        sms.setMobile(getMobile(request));

        String messageTemplate = (String) request.getAttribute(DEFAULT_ATTR_NAME_SMS_MESSAGE);
        sms.setMessage(new SmsMessage(messageTemplate, sms).toString());

        try {
            smsSender.send(sms);
            responseMessage(webRequest, verification.getExpireIn() + "");
        } catch (VerificationException e) {
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

    public void setSmsSender(SmsSender smsSender) {
        this.smsSender = smsSender;
    }
}
