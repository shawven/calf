
package com.test.security.browser.session;

import com.test.security.verification.Verification;
import com.test.security.verification.VerificationRepository;
import com.test.security.verification.VerificationType;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.web.context.request.ServletWebRequest;


/**
 * 基于session的验证码存取器
 *
 * @author Shoven
 * @since 2019-05-08 21:51
 */
public class SessionVerificationRepository implements VerificationRepository {

	/**
	 * 验证码放入session时的前缀
	 */
	public static String SESSION_KEY_PREFIX = "SESSION_KEY_FOR_CODE_";

	/**
	 * 操作session的工具类
	 */
	private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

	@Override
	public void save(ServletWebRequest request, Verification verification, VerificationType verificationCodeType) {
		sessionStrategy.setAttribute(request, getKey(request, verificationCodeType), verification);
	}

	@Override
	public Verification get(ServletWebRequest request, VerificationType verificationCodeType) {
		return (Verification) sessionStrategy.getAttribute(request, getKey(request, verificationCodeType));
	}

	@Override
	public void remove(ServletWebRequest request, VerificationType codeType) {
		sessionStrategy.removeAttribute(request, getKey(request, codeType));
	}

    @Override
    public String getKey(ServletWebRequest request, VerificationType verificationCodeType) {
        return SESSION_KEY_PREFIX + verificationCodeType.toString().toUpperCase();
    }
}
