
package com.test.security.verification.captcha;

import com.test.security.verification.VerificationGenerator;
import com.test.security.verification.properties.VerificationProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 默认的图片验证码生成器
 *
 */
public class CaptchaGenerator implements VerificationGenerator<Captcha> {

	/**
	 * 系统配置
	 */
	private VerificationProperties VerificationProperties;


    public CaptchaGenerator(VerificationProperties VerificationProperties) {
        this.VerificationProperties = VerificationProperties;
    }

    @Override
	public Captcha generate(ServletWebRequest request) {
		int width = ServletRequestUtils.getIntParameter(request.getRequest(), "width",
                VerificationProperties.getCaptcha().getWidth());
		int height = ServletRequestUtils.getIntParameter(request.getRequest(), "height",
                VerificationProperties.getCaptcha().getHeight());

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int fontSize = 20;

		Graphics g = image.getGraphics();
		g.setColor(getRandColor(200, 250));
		g.fillRect(0, 0, width, height);
		g.setFont(new Font("Times New Roman", Font.ITALIC, fontSize));
		g.setColor(getRandColor(160, 200));

        Random random = new Random();
		for (int i = 0, lineNum = (int)Math.sqrt(width * height); i < lineNum; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = x + random.nextInt(width / 3);
			int yl = y + random.nextInt(height / 3);
			g.drawLine(x, y, xl, yl);
		}

        String randomString = getRandomString();
        int len = randomString.length();
		int unitWidth = 13;
		int x = width / 2 - (unitWidth * len) / 2;
		int y = (height + fontSize / 2) / 2;

		for (int i = 0; i < len; i++) {
			g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            x += i == 0 ? 0 : unitWidth;
			g.drawString(String.valueOf(randomString.charAt(i)), x, y);
		}

		g.dispose();

		return new Captcha(image, randomString, VerificationProperties.getCaptcha().getExpireIn());
	}

	/**
	 * 生成随机背景条纹
	 *
	 * @param fc
	 * @param bc
	 * @return
	 */
	private Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255) {
			fc = 255;
		}
		if (bc > 255) {
			bc = 255;
		}
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	private String getRandomString() {
        String str;
        do {
            str = RandomStringUtils.randomAlphanumeric(VerificationProperties.getCaptcha().getLength());
            // 排除I l 1 这种模糊不清的
        } while (str.contains("I") || str.contains("l") || str.contains("1"));
        return str;
    }
}
