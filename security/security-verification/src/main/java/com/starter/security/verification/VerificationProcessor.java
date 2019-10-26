
package com.starter.security.verification;

import org.springframework.web.context.request.ServletWebRequest;

/**
 * 校验码处理器，封装不同校验码的处理逻辑
 */
public interface VerificationProcessor {

	/**
	 * 创建校验码
	 *
	 * @param request
	 * @throws Exception
	 */
	void create(ServletWebRequest request);

	/**
	 * 校验验证码
	 *
	 * @param servletWebRequest
	 * @throws Exception
	 */
	void verification(ServletWebRequest servletWebRequest);

}
