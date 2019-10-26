
package com.starter.security.verification;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.ServletWebRequest;


/**
 * 抽象的图片验证码处理器
 *
 * @author Shoven
 */
public abstract class AbstractVerificationProcessor<T extends Verification> implements VerificationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVerificationProcessor.class);

	private VerificationRepository verificationRepository;

	private VerificationGenerator<T> verificationGenerator;

    public AbstractVerificationProcessor(VerificationRepository verificationRepository,
                                         VerificationGenerator<T> verificationGenerator) {

        this.verificationRepository = verificationRepository;
        this.verificationGenerator = verificationGenerator;
    }

    @Override
	public void create(ServletWebRequest request) {
        try {
            T verification = verificationGenerator.generate(request);
            save(request, verification);
            send(request, verification);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void verification(ServletWebRequest request) {
        VerificationType codeType = getVerificationType(request);
        Verification verificationInSession = verificationRepository.get(request, codeType);

        String codeInRequest = request.getParameter(codeType.getLabel());
        if (StringUtils.isBlank(codeInRequest)) {
            throw new VerificationException("请输入" + codeType.getName() + "验证码");
        }

        if (verificationInSession == null || verificationInSession.isExpired()) {
            verificationRepository.remove(request, codeType);
            throw new VerificationException(codeType.getName() + "验证码已过期");
        }

        if (!StringUtils.equalsIgnoreCase(verificationInSession.getCode(), codeInRequest)) {
            throw new VerificationException(codeType.getName() + "验证码错误");
        }

        verificationRepository.remove(request, codeType);
    }

	/**
	 * 保存校验码
	 *
	 * @param request
	 * @param verification
	 */
	private void save(ServletWebRequest request, T verification) {
		Verification simpleVerification = new Verification(verification.getCode(), verification.getExpireTime());
		verificationRepository.save(request, simpleVerification, getVerificationType(request));
	}

	/**
	 * 发送校验码，由子类实现
	 *
	 * @param request
	 * @param verification
	 * @throws Exception
	 */
	protected abstract void send(ServletWebRequest request, T verification);

	/**
	 * 根据请求的url获取校验码的类型
	 *
	 * @param request
	 * @return
	 */
	public VerificationType getVerificationType(ServletWebRequest request) {
		String type = StringUtils.substringBefore(getClass().getSimpleName(), "CodeProcessor");
		return VerificationType.valueOf(type.toUpperCase());
	}
}
