
package com.test.security.validate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.ServletWebRequest;


/**
 * 抽象的图片验证码处理器
 *
 * @author Shoven
 */
public abstract class AbstractValidateCodeProcessor<T extends ValidateCode> implements ValidateCodeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractValidateCodeProcessor.class);

	private ValidateCodeRepository validateCodeRepository;

	private ValidateCodeGenerator validateCodeGenerator;

    public AbstractValidateCodeProcessor(ValidateCodeRepository validateCodeRepository,
                                         ValidateCodeGenerator validateCodeGenerator) {

        this.validateCodeRepository = validateCodeRepository;
        this.validateCodeGenerator = validateCodeGenerator;
    }

    @Override
	public void create(ServletWebRequest request) {
        try {
            T validateCode = (T)validateCodeGenerator.generate(request);
            save(request, validateCode);
            send(request, validateCode);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void validate(ServletWebRequest request) {
        ValidateCodeType codeType = getValidateCodeType(request);
        T codeInSession = (T) validateCodeRepository.get(request, codeType);

        String codeInRequest = request.getParameter(codeType.getLabel());
        if (StringUtils.isBlank(codeInRequest)) {
            throw new ValidateCodeException("请输入" + codeType.getName() + "验证码");
        }

        if (codeInSession == null || codeInSession.isExpired()) {
            validateCodeRepository.remove(request, codeType);
            throw new ValidateCodeException(codeType.getName() + "验证码已过期");
        }

        if (!StringUtils.equalsIgnoreCase(codeInSession.getCode(), codeInRequest)) {
            throw new ValidateCodeException(codeType.getName() + "验证码错误");
        }

        validateCodeRepository.remove(request, codeType);
    }

	/**
	 * 保存校验码
	 *
	 * @param request
	 * @param validateCode
	 */
	private void save(ServletWebRequest request, T validateCode) {
		ValidateCode code = new ValidateCode(validateCode.getCode(), validateCode.getExpireTime());
		validateCodeRepository.save(request, code, getValidateCodeType(request));
	}

	/**
	 * 发送校验码，由子类实现
	 *
	 * @param request
	 * @param validateCode
	 * @throws Exception
	 */
	protected abstract void send(ServletWebRequest request, T validateCode);

	/**
	 * 根据请求的url获取校验码的类型
	 *
	 * @param request
	 * @return
	 */
	public ValidateCodeType getValidateCodeType(ServletWebRequest request) {
		String type = StringUtils.substringBefore(getClass().getSimpleName(), "CodeProcessor");
		return ValidateCodeType.valueOf(type.toUpperCase());
	}
}
