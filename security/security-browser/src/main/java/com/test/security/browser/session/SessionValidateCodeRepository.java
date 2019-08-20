
package com.test.security.browser.session;

import com.test.security.validate.ValidateCode;
import com.test.security.validate.ValidateCodeRepository;
import com.test.security.validate.ValidateCodeType;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;


/**
 * 基于session的验证码存取器
 *
 * @author Shoven
 * @since 2019-05-08 21:51
 */
public class SessionValidateCodeRepository implements ValidateCodeRepository {

	/**
	 * 验证码放入session时的前缀
	 */
	public static String SESSION_KEY_PREFIX = "SESSION_KEY_FOR_CODE_";

	/**
	 * 操作session的工具类
	 */
	private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

	@Override
	public void save(ServletWebRequest request, ValidateCode code, ValidateCodeType validateCodeType) {
		sessionStrategy.setAttribute(request, getKey(request, validateCodeType), code);
	}

	@Override
	public ValidateCode get(ServletWebRequest request, ValidateCodeType validateCodeType) {
		return (ValidateCode) sessionStrategy.getAttribute(request, getKey(request, validateCodeType));
	}

	@Override
	public void remove(ServletWebRequest request, ValidateCodeType codeType) {
		sessionStrategy.removeAttribute(request, getKey(request, codeType));
	}

    @Override
    public String getKey(ServletWebRequest request, ValidateCodeType validateCodeType) {
        return SESSION_KEY_PREFIX + validateCodeType.toString().toUpperCase();
    }
}
