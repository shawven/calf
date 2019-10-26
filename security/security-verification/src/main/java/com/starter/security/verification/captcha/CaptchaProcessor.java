
package com.starter.security.verification.captcha;

import com.starter.security.verification.AbstractVerificationProcessor;
import com.starter.security.verification.VerificationGenerator;
import com.starter.security.verification.VerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.ServletWebRequest;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * 图片验证码处理器
        */
public class CaptchaProcessor extends AbstractVerificationProcessor<Captcha> {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaProcessor.class);

    public CaptchaProcessor(VerificationRepository verificationRepository,
                            VerificationGenerator verificationGenerator) {
        super(verificationRepository, verificationGenerator);
    }

    /**
	 * 发送图形验证码，将其写到响应中
	 */
	@Override
	protected void send(ServletWebRequest request, Captcha captcha) {
        try {
            ServletOutputStream outputStream = request.getResponse().getOutputStream();
            ImageIO.write(captcha.getImage(), "JPEG", outputStream);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


}
