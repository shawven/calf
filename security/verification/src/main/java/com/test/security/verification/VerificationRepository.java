
package com.test.security.verification;

import org.springframework.web.context.request.ServletWebRequest;

/**
 * 校验码存取器
 */
public interface VerificationRepository {

	/**
	 * 保存验证码
     *
	 * @param request
	 * @param verification
	 * @param verificationType
	 */
	void save(ServletWebRequest request, Verification verification, VerificationType verificationType);

	/**
	 * 获取验证码
     *
	 * @param request
	 * @param verificationType
	 * @return
	 */
    Verification get(ServletWebRequest request, VerificationType verificationType);

	/**
	 * 移除验证码
	 * @param request
	 * @param codeType
	 */
	void remove(ServletWebRequest request, VerificationType codeType);

    /**
     * 获取持久化的key
     *
     * @return
     */
	String getKey(ServletWebRequest request, VerificationType verificationType);
}
