
package com.test.security.verification;

import org.springframework.web.context.request.ServletWebRequest;

/**
 * 校验码生成器
 *
 */
public interface VerificationGenerator<T extends Verification> {

	/**
	 * 生成校验码
	 * @param request
	 * @return
	 */
    T generate(ServletWebRequest request);

}
