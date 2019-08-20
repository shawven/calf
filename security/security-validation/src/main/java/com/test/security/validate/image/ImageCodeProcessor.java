
package com.test.security.validate.image;

import com.test.security.validate.AbstractValidateCodeProcessor;
import com.test.security.validate.ValidateCodeGenerator;
import com.test.security.validate.ValidateCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.ServletWebRequest;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * 图片验证码处理器
 */
public class ImageCodeProcessor extends AbstractValidateCodeProcessor<ImageCode> {

    private static final Logger logger = LoggerFactory.getLogger(ImageCodeProcessor.class);

    public ImageCodeProcessor(ValidateCodeRepository validateCodeRepository,
                              ValidateCodeGenerator validateCodeGenerator) {
        super(validateCodeRepository, validateCodeGenerator);
    }

    /**
	 * 发送图形验证码，将其写到响应中
	 */
	@Override
	protected void send(ServletWebRequest request, ImageCode imageCode) {
        try {
            ServletOutputStream outputStream = request.getResponse().getOutputStream();
            ImageIO.write(imageCode.getImage(), "JPEG", outputStream);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


}
