
package com.test.security.validate;


import com.test.security.validate.property.ValidationConstants;

/**
 *
 * 校验码类型
 */
public enum ValidateCodeType {

	/**
	 * 短信验证码
	 */
	SMS(ValidationConstants.DEFAULT_PARAMETER_NAME_CODE_SMS, "短信"),

	/**
	 * 图片验证码
	 */
	IMAGE(ValidationConstants.DEFAULT_PARAMETER_NAME_CODE_IMAGE, "图形");

	private String label;

    private String name;

    ValidateCodeType(String label, String name) {
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
