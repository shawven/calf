
package com.starter.security.verification;


import com.starter.security.verification.properties.VerificationConstants;

/**
 *
 * 校验码类型
 */
public enum VerificationType {

	/**
	 * 短信验证码
	 */
	SMS(VerificationConstants.DEFAULT_PARAMETER_NAME_SMS, "短信"),

	/**
	 * 图片验证码
	 */
    CAPTCHA(VerificationConstants.DEFAULT_PARAMETER_NAME_CAPTCHA, "图形");

	private String label;

    private String name;

    VerificationType(String label, String name) {
        this.label = label;
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
